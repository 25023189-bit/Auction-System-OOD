package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Truy cập dữ liệu cho thao tác đặt giá trong một phiên đấu giá.
 *
 * Vai trò:
 * - Kiểm tra giá hiện tại, số dư bidder và bước giá trước khi ghi bid mới.
 * - Ghi bid transaction và cập nhật current price của item trong cùng transaction.
 *
 * Luồng chính:
 * 1. Khóa phiên đấu giá liên quan, đọc giá hiện tại, số dư và bid step.
 * 2. Xóa cờ highest cũ, insert bid mới, cập nhật giá item rồi commit.
 *
 * Business rules:
 * - Bid mới phải lớn hơn hoặc bằng current price cộng bid step.
 * - Bidder phải có số dư đủ cho số tiền bid, và chỉ một bid được đánh dấu highest tại một thời điểm.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe theo instance, nhưng transaction DB và FOR UPDATE bảo vệ luồng đặt giá đồng thời.
 * - Dependency: DatabaseConnection, JDBC, BidStatus, BidResult, SLF4J.
 */
public class BidDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidDAO.class);

    public BidResult placeBid(String auctionId, String bidderId, double bidAmount) {
        // Transaction này khóa auction để kiểm tra giá hiện tại và số dư trước khi ghi bid mới.
        String selectAuctionSql = """
                SELECT a.product_id, p.current_price, u.balance, a.min_bid_increment
                FROM auctions a
                JOIN products p ON a.product_id = p.product_id
                JOIN users u ON u.customer_id = ?
                WHERE a.auction_id = ?
                FOR UPDATE
                """;

        String clearHighestSql = """
                UPDATE bid_transactions
                SET is_highest = 0
                WHERE auction_id = ? AND is_highest = 1
                """;

        String insertBidSql = """
                INSERT INTO bid_transactions
                (auction_id, bidder_id, bid_amount, bid_rank, is_highest, bid_time)
                VALUES (?, ?, ?, (
                    SELECT next_rank FROM (
                        SELECT COALESCE(MAX(bid_rank), 0) + 1 AS next_rank
                        FROM bid_transactions
                        WHERE auction_id = ?
                    ) ranks
                ), ?, NOW())
                """;

        String updateItemPriceSql = """
                UPDATE products
                SET current_price = ?
                WHERE product_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String itemId;
            double currentPrice;
            double currentBalance;
            double bidStep;

            try (PreparedStatement pstmt = conn.prepareStatement(selectAuctionSql)) {
                pstmt.setString(1, bidderId);
                pstmt.setString(2, auctionId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return BidResult.fail(BidStatus.AUCTION_NOT_FOUND, "Auction not found.");
                    }

                    itemId = rs.getString("product_id");
                    currentPrice = rs.getDouble("current_price");
                    currentBalance = rs.getDouble("balance");
                    bidStep = rs.getDouble("min_bid_increment");
                }
            }

            // Giá mới phải cao hơn giá hiện tại ít nhất một bidStep.
            double minimumAllowedBid = currentPrice + bidStep;
            if (bidAmount < minimumAllowedBid) {
                conn.rollback();
                return BidResult.fail(BidStatus.BID_TOO_LOW, "Bid must increase by at least " + bidStep + " from the current price.");
            }

            if (bidAmount > currentBalance) {
                conn.rollback();
                return BidResult.fail(BidStatus.INSUFFICIENT_BALANCE, "Current balance is not enough for this bid amount.");
            }

            // Chỉ một bid được đánh dấu highest tại một thời điểm.
            try (PreparedStatement pstmt = conn.prepareStatement(clearHighestSql)) {
                pstmt.setString(1, auctionId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertBidSql)) {
                pstmt.setString(1, auctionId);
                pstmt.setString(2, bidderId);
                pstmt.setDouble(3, bidAmount);
                pstmt.setString(4, auctionId);
                pstmt.setInt(5, 1);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(updateItemPriceSql)) {
                pstmt.setDouble(1, bidAmount);
                pstmt.setString(2, itemId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return BidResult.success();
        } catch (SQLException e) {
            LOGGER.error("Database error while placing bid. auctionId={}, bidderId={}", auctionId, bidderId, e);
            return BidResult.fail(BidStatus.ERROR, "Database error while placing bid.");
        }
    }

    /**
     * Mã trạng thái nghiệp vụ của thao tác đặt giá.
     *
     * Vai trò:
     * - Chuẩn hóa lý do thành công/thất bại để service không phải parse chuỗi lỗi.
     * - Làm dữ liệu đi kèm trong BidResult.
     *
     * Luồng chính:
     * 1. BidDAO chọn BidStatus phù hợp khi validate hoặc thao tác DB thất bại.
     * 2. AuctionRoomService đọc status/message để trả response cho client.
     *
     * Business rules:
     * - SUCCESS chỉ dùng khi toàn bộ transaction đặt giá commit thành công.
     * - Các trạng thái fail phải phản ánh đúng nguyên nhân: không có phiên, giá thấp, thiếu số dư hoặc lỗi hệ thống.
     *
     * Ghi chú kỹ thuật:
     * - Thread-safe: enum bất biến theo thiết kế của Java.
     * - Dependency: Được dùng bởi BidDAO.BidResult và AuctionRoomService.
     */
    public enum BidStatus {
        SUCCESS,
        AUCTION_NOT_FOUND,
        BID_TOO_LOW,
        INSUFFICIENT_BALANCE,
        ERROR
    }

    /**
     * Giá trị kết quả trả về sau khi xử lý một yêu cầu đặt giá.
     *
     * Vai trò:
     * - Đóng gói success flag, BidStatus và thông báo lỗi nếu có.
     * - Tách kết quả transaction DB khỏi Message gửi ra client.
     *
     * Luồng chính:
     * 1. BidDAO tạo BidResult bằng success() hoặc fail().
     * 2. AuctionRoomService chuyển kết quả này thành BID_SUCCESS hoặc BID_FAIL.
     *
     * Business rules:
     * - success=true chỉ đi kèm BidStatus.SUCCESS.
     * - fail() luôn giữ message để tầng service có thể trả lý do cụ thể.
     *
     * Ghi chú kỹ thuật:
     * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
     * - Dependency: BidStatus và AuctionRoomService.
     */
    public static class BidResult {
        private final boolean success;
        private final BidStatus status;
        private final String message;

        private BidResult(boolean success, BidStatus status, String message) {
            this.success = success;
            this.status = status;
            this.message = message;
        }

        public static BidResult success() {
            return new BidResult(true, BidStatus.SUCCESS, null);
        }

        public static BidResult fail(BidStatus status, String message) {
            return new BidResult(false, status, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public BidStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

