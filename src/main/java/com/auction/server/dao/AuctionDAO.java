package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        String sql = """
                INSERT INTO auctions (
                    auction_id, item_id, seller_id, status,
                    start_time, duration_minutes, actual_end_time, extension_seconds,
                    starting_price, bid_step, minimum_join_amount
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomId());
            pstmt.setString(2, itemId);
            pstmt.setString(3, sellerId);
            pstmt.setString(4, room.getStatus() != null ? room.getStatus() : "OPEN");
            pstmt.setTimestamp(5, Timestamp.valueOf(room.getStartTime()));
            pstmt.setInt(6, room.getDurationMinutes());
            pstmt.setTimestamp(7, Timestamp.valueOf(room.getEndTime()));
            pstmt.setInt(8, room.getExtensionSeconds());
            pstmt.setDouble(9, room.getStartingPrice());
            pstmt.setDouble(10, room.getBidStep());
            pstmt.setDouble(11, room.getMinimumJoinAmount());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi luu Auction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId) {
        String insertItemSql = """
                INSERT INTO items (item_id, name, description, current_price)
                VALUES (?, ?, ?, ?)
                """;

        String insertAuctionSql = """
                INSERT INTO auctions (
                    auction_id, item_id, seller_id, status,
                    start_time, duration_minutes, actual_end_time, extension_seconds,
                    starting_price, bid_step, minimum_join_amount
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                itemStmt.setString(1, item.getId());
                itemStmt.setString(2, item.getProductName());
                itemStmt.setString(3, item.getDescription());
                itemStmt.setDouble(4, item.getStartingPrice());
                itemStmt.executeUpdate();
            }

            try (PreparedStatement auctionStmt = conn.prepareStatement(insertAuctionSql)) {
                auctionStmt.setString(1, room.getRoomId());
                auctionStmt.setString(2, item.getId());
                auctionStmt.setString(3, sellerId);
                auctionStmt.setString(4, room.getStatus() != null ? room.getStatus() : "OPEN");
                auctionStmt.setTimestamp(5, Timestamp.valueOf(room.getStartTime()));
                auctionStmt.setInt(6, room.getDurationMinutes());
                auctionStmt.setTimestamp(7, Timestamp.valueOf(room.getEndTime()));
                auctionStmt.setInt(8, room.getExtensionSeconds());
                auctionStmt.setDouble(9, room.getStartingPrice());
                auctionStmt.setDouble(10, room.getBidStep());
                auctionStmt.setDouble(11, room.getMinimumJoinAmount());
                auctionStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Transaction create auction failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<AuctionRoom> getAllActiveAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = """
                SELECT a.auction_id, a.item_id, a.seller_id, a.status,
                       a.start_time, a.duration_minutes, a.actual_end_time, a.extension_seconds,
                       a.starting_price, a.bid_step, a.minimum_join_amount,
                       i.name, i.description, i.current_price
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
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
            e.printStackTrace();
        }
        return list;
    }

    public List<AuctionRoom> getAllAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = """
                SELECT a.auction_id, a.item_id, a.seller_id, a.status,
                       a.start_time, a.duration_minutes, a.actual_end_time, a.extension_seconds,
                       a.starting_price, a.bid_step, a.minimum_join_amount,
                       i.name, i.description, i.current_price
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
                ORDER BY a.start_time DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapAuctionRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean forceDeleteAuction(String roomId) {
        String sql = "UPDATE auctions SET status = 'CANCELED_BY_ADMIN' WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AuctionRoom getAuctionById(String roomId) {
        String sql = """
                SELECT a.auction_id, a.item_id, a.seller_id, a.status,
                       a.start_time, a.duration_minutes, a.actual_end_time, a.extension_seconds,
                       a.starting_price, a.bid_step, a.minimum_join_amount,
                       i.name, i.description, i.current_price
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
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
            e.printStackTrace();
        }
        return null;
    }

    public SellerAuctionStats getSellerAuctionStats(String sellerId) {
        String sql = """
                SELECT
                    COUNT(*) AS total_count,
                    SUM(CASE WHEN status = 'SOLD' THEN 1 ELSE 0 END) AS sold_count,
                    SUM(CASE WHEN status = 'CANCELED_BY_ADMIN' THEN 1 ELSE 0 END) AS admin_canceled_count
                FROM auctions
                WHERE seller_id = ?
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
            System.err.println("Failed to load seller auction stats: " + e.getMessage());
            e.printStackTrace();
        }

        return new SellerAuctionStats(0, 0, 0);
    }

    public boolean closeAuctionBySeller(String roomId, String sellerId) {
        String sql = """
                UPDATE auctions
                SET status = 'CLOSED_BY_SELLER'
                WHERE auction_id = ? AND seller_id = ? AND status IN ('OPEN', 'RUNNING')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            pstmt.setString(2, sellerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi dong phien boi seller: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public CloseAuctionResult closeAuctionByTime(String roomId) {
        String auctionSql = """
                SELECT auction_id, seller_id, status
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

            String sellerId;
            String currentStatus;

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
                    pstmt.setString(1, "UNSOLD");
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
                pstmt.setString(1, "SOLD");
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
            System.err.println("Loi chot phien theo thoi gian: " + e.getMessage());
            e.printStackTrace();
            return CloseAuctionResult.fail("Database error while finalizing auction.");
        }
    }

    private AuctionRoom mapAuctionRoom(ResultSet rs) throws SQLException {
        AuctionRoom room = new AuctionRoom();

        room.setRoomId(rs.getString("auction_id"));
        room.setItemId(rs.getString("item_id"));
        room.setSellerName(rs.getString("seller_id"));
        room.setStatus(rs.getString("status"));
        room.setItemName(rs.getString("name"));
        room.setItemDescription(rs.getString("description"));
        room.setCurrentPrice(rs.getDouble("current_price"));
        room.setStartingPrice(rs.getDouble("starting_price"));
        room.setBidStep(rs.getDouble("bid_step"));
        room.setMinimumJoinAmount(rs.getDouble("minimum_join_amount"));

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            room.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("actual_end_time");
        if (endTs != null) {
            room.setEndTime(endTs.toLocalDateTime());
        }

        room.setDurationMinutes(rs.getInt("duration_minutes"));
        room.setExtensionSeconds(rs.getInt("extension_seconds"));
        applySellerStats(room);

        return room;
    }

    private void applySellerStats(AuctionRoom room) {
        if (room == null || room.getSellerName() == null || room.getSellerName().isBlank()) {
            return;
        }

        SellerAuctionStats stats = getSellerAuctionStats(room.getSellerName());
        room.setSellerReputation(5.0);
        room.setSellerSuccessfulAuctionRate(stats.getSuccessfulAuctionRate());
        room.setSellerAdminCancellationRate(stats.getAdminCancellationRate());
    }

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

