package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu cho thao tác đặt giá trong một phiên đấu giá.
 * Triển khai interface IBidDAO để phục vụ Mocking và Dependency Injection.
 */
public class BidDAO implements IBidDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidDAO.class);

    @Override
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
                        return BidResult.fail("Auction not found.");
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
                return BidResult.fail("Bid must increase by at least " + bidStep + " from the current price.");
            }

            if (bidAmount > currentBalance) {
                conn.rollback();
                return BidResult.fail("Current balance is not enough for this bid amount.");
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
            return BidResult.fail("Database error while placing bid.");
        }
    }

    @Override
    public List<com.auction.common.model.BidTransaction> getHistoryByRoom(String roomId) {
        List<com.auction.common.model.BidTransaction> list = new ArrayList<>();
        String sql = """
                SELECT bid_id, auction_id, bidder_id, bid_amount, bid_rank, is_highest, bid_time
                FROM bid_transactions
                WHERE auction_id = ?
                ORDER BY bid_amount DESC
                """;

        // Khai báo Formatter với đường dẫn thư viện đầy đủ để không bị lỗi đỏ
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    com.auction.common.model.BidTransaction tx = new com.auction.common.model.BidTransaction();

                    // Đã sửa thành setTransactionId để khớp với Model của bạn
                    tx.setTransactionId(rs.getInt("bid_id"));
                    tx.setAuctionId(rs.getString("auction_id"));
                    tx.setBidderId(rs.getString("bidder_id"));
                    tx.setBidAmount(rs.getDouble("bid_amount"));
                    tx.setBidRank(rs.getInt("bid_rank"));
                    tx.setHighest(rs.getInt("is_highest") == 1);

                    // Xử lý convert thời gian sang chuỗi chuẩn xác
                    if (rs.getTimestamp("bid_time") != null) {
                        java.time.LocalDateTime dateTime = rs.getTimestamp("bid_time").toLocalDateTime();
                        tx.setBidTime(dtf.format(dateTime));
                    }

                    list.add(tx);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi lấy lịch sử đặt giá của phòng: {}", roomId, e);
        }
        return list;
    }
}