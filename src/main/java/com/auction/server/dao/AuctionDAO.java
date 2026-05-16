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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu cho phiên đấu giá, sản phẩm liên quan và thao tác chốt phiên.
 *
 * Vai trò:
 * - Tạo, đọc, hủy mềm và đóng phiên đấu giá trong database.
 * - Gom dữ liệu auctions/products thành AuctionRoom và thống kê hiệu quả của seller.
 *
 * Luồng chính:
 * 1. Nhận yêu cầu từ service/handler, mở JDBC connection và thực thi SQL tương ứng.
 * 2. Map ResultSet về model dùng chung hoặc trả về kết quả nghiệp vụ cho tầng gọi.
 *
 * Business rules:
 * - Tạo auction kèm item phải nằm trong cùng một transaction để tránh lệch dữ liệu.
 * - Chốt phiên hết giờ phải khóa auction, xác định bid cao nhất, chuyển tiền và cập nhật trạng thái atomically.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe theo instance, nhưng mỗi method dùng connection local nên có thể gọi đồng thời nếu DB chịu tải.
 * - Dependency: DatabaseConnection, AuctionRoom, Item, JDBC, SLF4J.
 */
public class AuctionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionDAO.class);

    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        // Cập nhật theo Schema V4.3: dùng product_id, created_by, end_time, min_bid_increment
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
            pstmt.setTimestamp(7, Timestamp.valueOf(room.getEndTime())); // Khởi tạo actual_end_time = end_time
            pstmt.setDouble(8, room.getBidStep());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to save auction.", e);
            return false;
        }
    }

    public boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId) {
        // Cập nhật bảng products chuẩn theo DB Version 4.2
        String insertItemSql = """
                INSERT INTO products (product_name, description, starting_price, current_price, seller_id, product_type)
                VALUES (?, ?, ?, ?, ?, 'ELECTRONICS')
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

            int productId;
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {
                itemStmt.setString(1, item.getProductName());
                itemStmt.setString(2, item.getDescription());
                itemStmt.setDouble(3, item.getStartingPrice());
                itemStmt.setDouble(4, item.getStartingPrice());
                itemStmt.setString(5, sellerId);
                itemStmt.executeUpdate();

                try (ResultSet generatedKeys = itemStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        conn.rollback();
                        LOGGER.error("Transaction create auction failed: generated product_id was not returned.");
                        return false;
                    }
                    productId = generatedKeys.getInt(1);
                }
            }

            try (PreparedStatement auctionStmt = conn.prepareStatement(insertAuctionSql)) {
                auctionStmt.setString(1, room.getRoomId());
                auctionStmt.setInt(2, productId);
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

    public String generateNextAuctionId() {
        String sql = """
                SELECT auction_id
                FROM auctions
                WHERE auction_id REGEXP '^AU1[0-9]{5}$'
                ORDER BY auction_id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("auction_id");
                int nextNumber = Integer.parseInt(lastId.substring(3)) + 1;
                return "AU1" + String.format("%05d", nextNumber);
            }

            return "AU100001";
        } catch (SQLException | NumberFormatException e) {
            LOGGER.error("Failed to generate next auction id.", e);
            return "AU1" + String.format("%05d", System.currentTimeMillis() % 100000);
        }
    }

    public List<AuctionRoom> getAllActiveAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        // Lobby chỉ hiển thị phiên còn mở/chạy và chưa quá thời điểm kết thúc.
        String sql = """
                SELECT a.auction_id, a.product_id, a.created_by AS seller_id, a.status,
                       a.start_time, a.end_time, a.actual_end_time, a.min_bid_increment,
                       p.product_name, p.description, p.current_price, p.starting_price
                FROM auctions a
                JOIN products p ON a.product_id = p.product_id
                WHERE a.status IN ('OPEN', 'RUNNING')
                  AND COALESCE(a.actual_end_time, a.end_time) > NOW(3)
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

    public List<AuctionRoom> getAllOpenOrRunningAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        // Watcher cần thấy cả các phiên quá giờ để còn chốt trạng thái trong DB.
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
            LOGGER.error("Failed to load open or running auctions.", e);
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
        String sql = "UPDATE auctions SET status = 'CANCELED' WHERE auction_id = ?"; // Đổi CANCELED_BY_ADMIN thành CANCELED theo ENUM
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
                SET status = ?,
                    winner_id = ?,
                    final_price = ?,
                    actual_end_time = NOW(3)
                WHERE auction_id = ?
                """;

        // BidDAO và UI dùng users.balance, nên chốt phiên cũng cập nhật cùng nguồn số dư.
        String debitWinnerSql = """
                UPDATE users
                SET balance = balance - ?
                WHERE customer_id = ? AND balance >= ?
                """;

        String creditSellerSql = """
                UPDATE users
                SET balance = balance + ?
                WHERE customer_id = ?
                """;

        String selectBalancesSql = """
                SELECT customer_id, balance
                FROM users
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
                    pstmt.setString(2, null);
                    pstmt.setNull(3, java.sql.Types.DECIMAL);
                    pstmt.setString(4, roomId);
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
                pstmt.setString(1, "PAID"); // Đổi SOLD thành PAID theo ENUM
                pstmt.setString(2, winnerId);
                pstmt.setDouble(3, finalPrice);
                pstmt.setString(4, roomId);
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

        // Cột không tồn tại ở DB nữa, set mặc định để logic phía trên không vỡ
        room.setMinimumJoinAmount(0.0);
        room.setDurationMinutes(0);
        room.setExtensionSeconds(0);

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            room.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("actual_end_time");
        if (endTs == null) {
            endTs = rs.getTimestamp("end_time");
        }
        if (endTs != null) {
            room.setEndTime(endTs.toLocalDateTime());
        }

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
     * Giá trị kết quả trả về sau khi chốt phiên đấu giá.
     *
     * Vai trò:
     * - Mang trạng thái cuối cùng của phiên sau khi closeAuctionByTime() xử lý.
     * - Cung cấp dữ liệu số dư winner/seller để service broadcast lại cho client.
     *
     * Luồng chính:
     * 1. AuctionDAO tạo instance thông qua factory sold(), unsold() hoặc fail().
     * 2. AuctionRoomService đọc các getter để quyết định message và event cần phát.
     *
     * Business rules:
     * - SOLD chỉ hợp lệ khi có winner và giao dịch chuyển tiền thành công.
     * - UNSOLD là kết quả thành công nhưng không phát sinh winner hoặc thanh toán.
     *
     * Ghi chú kỹ thuật:
     * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
     * - Dependency: Không phụ thuộc DB trực tiếp; là DTO nội bộ của AuctionDAO/AuctionRoomService.
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
     * Thống kê hiệu quả đấu giá của một seller.
     *
     * Vai trò:
     * - Lưu tổng số phiên, số phiên bán thành công và số phiên bị admin hủy.
     * - Tính tỷ lệ thành công/tỷ lệ bị hủy để đưa vào AuctionRoom hoặc User.
     *
     * Luồng chính:
     * 1. AuctionDAO truy vấn aggregate theo sellerId và tạo SellerAuctionStats.
     * 2. Tầng service/handler đọc tỷ lệ để hiển thị hoặc validate yêu cầu tạo phiên.
     *
     * Business rules:
     * - Số lượng âm được chuẩn hóa về 0 khi khởi tạo.
     * - Nếu seller chưa có phiên nào thì các tỷ lệ trả về 0.0 để tránh chia cho 0.
     *
     * Ghi chú kỹ thuật:
     * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
     * - Dependency: Không phụ thuộc ngoài; được tạo từ dữ liệu aggregate của AuctionDAO.
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

        public int getTotalAuctions() {
            return totalAuctions;
        }

        public int getSoldAuctions() {
            return soldAuctions;
        }

        public int getAdminCanceledAuctions() {
            return adminCanceledAuctions;
        }

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
