package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác với bảng auctions/products và các nghiệp vụ chốt phiên đấu giá.
 */
public class AuctionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionDAO.class);

    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        // Lưu một phiên đấu giá khi item đã tồn tại trước đó.
        String sql = """
                INSERT INTO auctions (
                    auction_id, product_id, created_by, status,
                    start_time, end_time, actual_end_time,
                    min_bid_increment
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomId());
            pstmt.setInt(2, Integer.parseInt(itemId)); // DB lưu product_id dạng INT.
            pstmt.setString(3, sellerId);
            pstmt.setString(4, room.getStatus() != null ? room.getStatus() : "OPEN");
            pstmt.setTimestamp(5, Timestamp.valueOf(room.getStartTime()));
            pstmt.setTimestamp(6, Timestamp.valueOf(room.getEndTime()));
            pstmt.setTimestamp(7, Timestamp.valueOf(room.getEndTime())); // actual_end_time ban đầu bằng end_time.
            pstmt.setDouble(8, room.getBidStep());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to save auction.", e);
            return false;
        }
    }

    public boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId) {
        // Tạo product và auction trong cùng transaction để tránh lệch dữ liệu.
        String insertItemSql = """
                INSERT INTO products (product_id, product_name, description, starting_price, current_price, seller_id, product_type)
                VALUES (?, ?, ?, ?, ?, ?, 'ELECTRONICS')
                """;

        String insertAuctionSql = """
                INSERT INTO auctions (
                    auction_id, product_id, created_by, status,
                    start_time, end_time, actual_end_time,
                    min_bid_increment
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                itemStmt.setInt(1, Integer.parseInt(item.getId())); // DB lưu product_id dạng INT.
                itemStmt.setString(2, item.getProductName());
                itemStmt.setString(3, item.getDescription());
                itemStmt.setDouble(4, item.getStartingPrice());
                itemStmt.setDouble(5, item.getStartingPrice()); // current_price khởi tạo bằng starting_price.
                itemStmt.setString(6, sellerId);
                itemStmt.executeUpdate();
            }

            try (PreparedStatement auctionStmt = conn.prepareStatement(insertAuctionSql)) {
                auctionStmt.setString(1, room.getRoomId());
                auctionStmt.setInt(2, Integer.parseInt(item.getId()));
                auctionStmt.setString(3, sellerId);
                auctionStmt.setString(4, room.getStatus() != null ? room.getStatus() : "OPEN");
                auctionStmt.setTimestamp(5, Timestamp.valueOf(room.getStartTime()));
                auctionStmt.setTimestamp(6, Timestamp.valueOf(room.getEndTime()));
                auctionStmt.setTimestamp(7, Timestamp.valueOf(room.getEndTime()));
                auctionStmt.setDouble(8, room.getBidStep());
                auctionStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Transaction create auction failed.", e);
            return false;
        }
    }

    public List<AuctionRoom> getAllActiveAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        // Lobby chỉ lấy các phiên còn có thể hiển thị/tham gia.
        String sql = """
                SELECT a.auction_id, a.product_id, a.created_by AS seller_id, a.status,
                       a.start_time, a.end_time, a.actual_end_time, a.min_bid_increment,
                       p.product_name, p.description, p.current_price, p.starting_price
                FROM auctions a
                JOIN products p ON a.product_id = p.product_id
                WHERE a.status IN ('OPEN', 'RUNNING')
                ORDER BY a.start_time ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapAuctionRoom(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load active auctions.", e);
        }
        return list;
    }

    public List<AuctionRoom> getAllAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = """
                SELECT a.auction_id, a.product_id, a.created_by AS seller_id, a.status,
                       a.start_time, a.end_time, a.actual_end_time, a.min_bid_increment,
                       p.product_name, p.description, p.current_price, p.starting_price
                FROM auctions a
                JOIN products p ON a.product_id = p.product_id
                ORDER BY a.start_time DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapAuctionRoom(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load auctions.", e);
        }
        return list;
    }

    public boolean forceDeleteAuction(String roomId) {
        // Admin không xóa vật lý mà chuyển trạng thái để giữ lịch sử.
        String sql = "UPDATE auctions SET status = 'CANCELED' WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to force delete auction {}.", roomId, e);
            return false;
        }
    }

    public AuctionRoom getAuctionById(String roomId) {
        String sql = """
                SELECT a.auction_id, a.product_id, a.created_by AS seller_id, a.status,
                       a.start_time, a.end_time, a.actual_end_time, a.min_bid_increment,
                       p.product_name, p.description, p.current_price, p.starting_price
                FROM auctions a
                JOIN products p ON a.product_id = p.product_id
                WHERE a.auction_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionRoom(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load auction by id {}.", roomId, e);
        }
        return null;
    }

    public SellerAuctionStats getSellerAuctionStats(String sellerId) {
        // Thống kê dùng để đánh giá seller khi tạo phiên mới.
        String sql = """
                SELECT
                    COUNT(*) AS total_count,
                    SUM(CASE WHEN status IN ('FINISHED', 'PAID') THEN 1 ELSE 0 END) AS sold_count,
                    SUM(CASE WHEN status = 'CANCELED' THEN 1 ELSE 0 END) AS admin_canceled_count
                FROM auctions
                WHERE created_by = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sellerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalCount = rs.getInt("total_count");
                    int soldCount = rs.getInt("sold_count");
                    int adminCanceledCount = rs.getInt("admin_canceled_count");
                    return new SellerAuctionStats(totalCount, soldCount, adminCanceledCount);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load seller auction stats for {}.", sellerId, e);
        }

        return new SellerAuctionStats(0, 0, 0);
    }

    public boolean closeAuctionBySeller(String roomId, String sellerId) {
        // Seller chỉ được đóng phiên do chính mình tạo và phiên vẫn đang mở/chạy.
        String sql = """
                UPDATE auctions
                SET status = 'CANCELED'
                WHERE auction_id = ? AND created_by = ? AND status IN ('OPEN', 'RUNNING')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            pstmt.setString(2, sellerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to close auction {} by seller {}.", roomId, sellerId, e);
            return false;
        }
    }

    public CloseAuctionResult closeAuctionByTime(String roomId) {
        // Chốt phiên hết giờ trong transaction: khóa auction, tìm bid cao nhất, chuyển tiền và cập nhật status.
        String auctionSql = """
                SELECT auction_id, created_by AS seller_id, status
                FROM auctions
                WHERE auction_id = ?
                FOR UPDATE
                """;

        String highestBidSql = """
                SELECT bidder_id, bid_amount
                FROM bid_transactions
                WHERE auction_id = ? AND is_highest = 1
                ORDER BY bid_amount DESC, bid_time ASC
                LIMIT 1
                """;

        String updateAuctionStatusSql = """
                UPDATE auctions
                SET status = ?
                WHERE auction_id = ?
                """;

        // Wallet winner bị trừ và wallet seller được cộng trong cùng transaction.
        String debitWinnerSql = """
                UPDATE wallets
                SET balance = balance - ?
                WHERE customer_id = ? AND balance >= ?
                """;

        String creditSellerSql = """
                UPDATE wallets
                SET balance = balance + ?
                WHERE customer_id = ?
                """;

        String selectBalancesSql = """
                SELECT customer_id, balance
                FROM wallets
                WHERE customer_id IN (?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String sellerId = null;
            String currentStatus = null;

            try (PreparedStatement pstmt = conn.prepareStatement(auctionSql)) {
                pstmt.setString(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return CloseAuctionResult.fail("Auction not found.");
                    }
                    sellerId = rs.getString("seller_id");
                    currentStatus = rs.getString("status");
                }
            }

            if (currentStatus != null && !("OPEN".equalsIgnoreCase(currentStatus) || "RUNNING".equalsIgnoreCase(currentStatus))) {
                conn.rollback();
                return CloseAuctionResult.fail("Auction is already in a finished state.");
            }

            String winnerId = null;
            double finalPrice = 0.0;

            try (PreparedStatement pstmt = conn.prepareStatement(highestBidSql)) {
                pstmt.setString(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        winnerId = rs.getString("bidder_id");
                        finalPrice = rs.getDouble("bid_amount");
                    }
                }
            }

            if (winnerId == null) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateAuctionStatusSql)) {
                    pstmt.setString(1, "FINISHED"); // Không có winner: phiên kết thúc nhưng không phát sinh thanh toán.
                    pstmt.setString(2, roomId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return CloseAuctionResult.unsold();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(debitWinnerSql)) {
                pstmt.setDouble(1, finalPrice);
                pstmt.setString(2, winnerId);
                pstmt.setDouble(3, finalPrice);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return CloseAuctionResult.fail("Winner does not have enough balance to finalize the auction.");
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(creditSellerSql)) {
                pstmt.setDouble(1, finalPrice);
                pstmt.setString(2, sellerId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(updateAuctionStatusSql)) {
                pstmt.setString(1, "PAID"); // Có winner và đã chuyển tiền thành công.
                pstmt.setString(2, roomId);
                pstmt.executeUpdate();
            }

            Double winnerBalance = null;
            Double sellerBalance = null;

            try (PreparedStatement pstmt = conn.prepareStatement(selectBalancesSql)) {
                pstmt.setString(1, winnerId);
                pstmt.setString(2, sellerId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String customerId = rs.getString("customer_id");
                        double balance = rs.getDouble("balance");

                        if (customerId.equalsIgnoreCase(winnerId)) {
                            winnerBalance = balance;
                        } else if (customerId.equalsIgnoreCase(sellerId)) {
                            sellerBalance = balance;
                        }
                    }
                }
            }

            conn.commit();
            return CloseAuctionResult.sold(winnerId, sellerId, finalPrice, winnerBalance, sellerBalance);

        } catch (SQLException e) {
            LOGGER.error("Failed to finalize auction {} by time.", roomId, e);
            return CloseAuctionResult.fail("Database error while finalizing auction.");
        }
    }

    // Map ResultSet từ JOIN auctions/products sang AuctionRoom dùng chung cho client.
    private AuctionRoom mapAuctionRoom(ResultSet rs) throws SQLException {
        AuctionRoom room = new AuctionRoom();

        room.setRoomId(rs.getString("auction_id"));
        room.setItemId(String.valueOf(rs.getInt("product_id")));
        room.setSellerName(rs.getString("seller_id"));
        room.setStatus(rs.getString("status"));
        room.setItemName(rs.getString("product_name"));
        room.setItemDescription(rs.getString("description"));
        room.setCurrentPrice(rs.getDouble("current_price"));
        room.setStartingPrice(rs.getDouble("starting_price"));
        room.setBidStep(rs.getDouble("min_bid_increment"));

        // Cột này không còn trong DB hiện tại, giữ mặc định để model client không null.
        room.setMinimumJoinAmount(0.0);

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            room.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("actual_end_time");
        if (endTs != null) {
            room.setEndTime(endTs.toLocalDateTime());
        }

        // Các giá trị thời lượng/gia hạn runtime được service bổ sung sau.
        room.setDurationMinutes(0);
        room.setExtensionSeconds(0);

        applySellerStats(room);

        return room;
    }

    // Gắn thống kê seller vào room để client/admin có dữ liệu đánh giá.
    private void applySellerStats(AuctionRoom room) {
        if (room == null || room.getSellerName() == null || room.getSellerName().isBlank()) {
            return;
        }

        SellerAuctionStats stats = getSellerAuctionStats(room.getSellerName());
        room.setSellerReputation(5.0);
        room.setSellerSuccessfulAuctionRate(stats.getSuccessfulAuctionRate());
        room.setSellerAdminCancellationRate(stats.getAdminCancellationRate());
    }

    /**
     * Kết quả chốt phiên đấu giá sau khi hết thời gian.
     */
    public static class CloseAuctionResult {
        private final boolean success;
        private final String finalStatus;
        private final String winnerId;
        private final String sellerId;
        private final double finalPrice;
        private final Double winnerBalance;
        private final Double sellerBalance;
        private final String message;

        private CloseAuctionResult(boolean success, String finalStatus, String winnerId, String sellerId,
                                   double finalPrice, Double winnerBalance, Double sellerBalance, String message) {
            this.success = success;
            this.finalStatus = finalStatus;
            this.winnerId = winnerId;
            this.sellerId = sellerId;
            this.finalPrice = finalPrice;
            this.winnerBalance = winnerBalance;
            this.sellerBalance = sellerBalance;
            this.message = message;
        }

        public static CloseAuctionResult sold(String winnerId, String sellerId, double finalPrice,
                                              Double winnerBalance, Double sellerBalance) {
            return new CloseAuctionResult(true, "SOLD", winnerId, sellerId, finalPrice, winnerBalance, sellerBalance,
                    "Auction sold successfully.");
        }

        public static CloseAuctionResult unsold() {
            return new CloseAuctionResult(true, "UNSOLD", null, null, 0.0, null, null,
                    "Auction ended without a buyer.");
        }

        public static CloseAuctionResult fail(String message) {
            return new CloseAuctionResult(false, "ERROR", null, null, 0.0, null, null, message);
        }

        public boolean isSuccess() { return success; }
        public String getFinalStatus() { return finalStatus; }
        public String getWinnerId() { return winnerId; }
        public String getSellerId() { return sellerId; }
        public double getFinalPrice() { return finalPrice; }
        public Double getWinnerBalance() { return winnerBalance; }
        public Double getSellerBalance() { return sellerBalance; }
        public String getMessage() { return message; }
    }

    /**
     * Thống kê hiệu quả phiên đấu giá của seller.
     */
    public static class SellerAuctionStats {
        private final int totalAuctions;
        private final int soldAuctions;
        private final int adminCanceledAuctions;

        public SellerAuctionStats(int totalAuctions, int soldAuctions, int adminCanceledAuctions) {
            this.totalAuctions = Math.max(totalAuctions, 0);
            this.soldAuctions = Math.max(soldAuctions, 0);
            this.adminCanceledAuctions = Math.max(adminCanceledAuctions, 0);
        }

        public int getTotalAuctions() { return totalAuctions; }
        public int getSoldAuctions() { return soldAuctions; }
        public int getAdminCanceledAuctions() { return adminCanceledAuctions; }

        public double getSuccessfulAuctionRate() {
            if (totalAuctions == 0) return 0.0;
            return (double) soldAuctions / totalAuctions;
        }

        public double getAdminCancellationRate() {
            if (totalAuctions == 0) return 0.0;
            return (double) adminCanceledAuctions / totalAuctions;
        }
    }
}
