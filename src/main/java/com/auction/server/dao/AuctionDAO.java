package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.server.utils.DatabaseConnection;

import java.sql.*;

public class AuctionDAO {

    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        String initialStatus = "RUNNING";

        // Nếu thời gian bắt đầu ở tương lai thì phiên chưa chạy ngay
        if (room.getStartTime() != null && room.getStartTime().isAfter(java.time.LocalDateTime.now())) {
            initialStatus = "SCHEDULED";
        }

        String sql = "INSERT INTO auctions (auction_id, item_id, seller_id, status, start_time, duration_minutes, actual_end_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomId());
            pstmt.setString(2, itemId);
            pstmt.setString(3, sellerId);
            pstmt.setString(4, initialStatus);

            if (room.getStartTime() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(room.getStartTime()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            pstmt.setInt(6, room.getDurationMinutes());

            if (room.getActualEndTime() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(room.getActualEndTime()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<AuctionRoom> getAllActiveAuctions() {
        java.util.List<AuctionRoom> list = new java.util.ArrayList<>();

        String sql = "SELECT a.auction_id, i.name AS item_name, i.current_price, u.username AS seller_name, " +
                "a.status, a.start_time, a.duration_minutes, a.actual_end_time " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.item_id " +
                "JOIN users u ON a.seller_id = u.customer_id " +
                "WHERE a.status IN ('RUNNING', 'SCHEDULED')";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String roomId = rs.getString("auction_id");
                String itemName = rs.getString("item_name");
                double currentPrice = rs.getDouble("current_price");
                String sellerName = rs.getString("seller_name");
                String status = rs.getString("status");

                AuctionRoom room = new AuctionRoom(roomId, itemName, currentPrice, sellerName);
                room.setStatus(status);

                java.sql.Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) {
                    room.setStartTime(startTs.toLocalDateTime());
                }

                room.setDurationMinutes(rs.getInt("duration_minutes"));

                java.sql.Timestamp endTs = rs.getTimestamp("actual_end_time");
                if (endTs != null) {
                    room.setActualEndTime(endTs.toLocalDateTime());
                }

                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                // Tự đồng bộ trạng thái theo thời gian
                if (room.getActualEndTime() != null && now.isAfter(room.getActualEndTime())) {
                    closeAuctionByTime(roomId);
                    continue;
                }

                if ("SCHEDULED".equalsIgnoreCase(room.getStatus())
                        && room.getStartTime() != null
                        && (now.isEqual(room.getStartTime()) || now.isAfter(room.getStartTime()))) {
                    updateAuctionStatus(roomId, "RUNNING");
                    room.setStatus("RUNNING");
                }

                list.add(room);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi SQL getAllActiveAuctions: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Chốt đơn bằng Transaction: Không xóa phòng mà cập nhật trạng thái
     * [0]: boolean (Thành công hay không)
     * [1]: String (ID người thắng - null nếu ế)
     * [2]: Double (Giá chốt)
     */
    public Object[] closeAuctionAndTransferMoney(String roomId, String sellerId) {
        String getHighestBidSql = "SELECT bidder_id, bid_amount FROM bid_transactions WHERE auction_id = ? ORDER BY bid_amount DESC LIMIT 1";
        String deductBuyerSql = "UPDATE users SET balance = balance - ? WHERE customer_id = ? AND balance >= ?";
        String addSellerSql = "UPDATE users SET balance = balance + ? WHERE customer_id = ?";

        // 🌟 SỬA Ở ĐÂY: Thay lệnh DELETE bằng UPDATE status
        String updateAuctionStatusSql = "UPDATE auctions SET status = ? WHERE auction_id = ?";

        java.sql.Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION TRỪ TIỀN

            // 1. Tìm người thắng
            String winnerId = null;
            double finalPrice = 0.0;
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(getHighestBidSql)) {
                pstmt.setString(1, roomId);
                try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        winnerId = rs.getString("bidder_id");
                        finalPrice = rs.getDouble("bid_amount");
                    }
                }
            }

            String finalStatus = "UNSOLD"; // Mặc định là ế hàng

            if (winnerId != null) {
                // 2. Trừ tiền người mua
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(deductBuyerSql)) {
                    pstmt.setDouble(1, finalPrice);
                    pstmt.setString(2, winnerId);
                    pstmt.setDouble(3, finalPrice);
                    int updatedRows = pstmt.executeUpdate();

                    // Nếu updatedRows = 0 nghĩa là người mua không có đủ tiền
                    if (updatedRows == 0) {
                        conn.rollback(); // Hủy giao dịch lập tức!
                        return new Object[]{false, winnerId, finalPrice, "Người mua không đủ số dư để thanh toán!"};
                    }
                }

                // 3. Cộng tiền cho người bán
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(addSellerSql)) {
                    pstmt.setDouble(1, finalPrice);
                    pstmt.setString(2, sellerId);
                    pstmt.executeUpdate();
                }

                finalStatus = "SOLD"; // Có người mua -> Đổi trạng thái thành đã bán
            }

            // 4. LƯU LẠI LỊCH SỬ PHÒNG (Cập nhật status thay vì Xóa)
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(updateAuctionStatusSql)) {
                pstmt.setString(1, finalStatus); // Set thành 'SOLD' hoặc 'UNSOLD'
                pstmt.setString(2, roomId);
                pstmt.executeUpdate();
            }

            conn.commit(); // HOÀN TẤT GIAO DỊCH
            return new Object[]{true, winnerId, finalPrice, "Chốt đơn thành công!"};

        } catch (java.sql.SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (java.sql.SQLException ex) {}
            e.printStackTrace();
            return new Object[]{false, null, 0.0, "Lỗi Database!"};
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (java.sql.SQLException e) {}
        }
    }

    /**
     * Lấy thông tin chi tiết của MỘT phòng đấu giá bằng ID
     */
    public AuctionRoom getAuctionById(String roomId) {
        String sql = "SELECT a.auction_id, i.name AS item_name, i.description AS item_description, " +
                "i.current_price, u.username AS seller_name, " +
                "a.status, a.start_time, a.duration_minutes, a.actual_end_time " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.item_id " +
                "JOIN users u ON a.seller_id = u.customer_id " +
                "WHERE a.auction_id = ?";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AuctionRoom room = new AuctionRoom(
                            rs.getString("auction_id"),
                            rs.getString("item_name"),
                            rs.getDouble("current_price"),
                            rs.getString("seller_name")
                    );

                    room.setItemDescription(rs.getString("item_description"));

                    room.setStatus(rs.getString("status"));
                    room.setDurationMinutes(rs.getInt("duration_minutes"));

                    java.sql.Timestamp startTs = rs.getTimestamp("start_time");
                    if (startTs != null) {
                        room.setStartTime(startTs.toLocalDateTime());
                    }

                    java.sql.Timestamp endTs = rs.getTimestamp("actual_end_time");
                    if (endTs != null) {
                        room.setActualEndTime(endTs.toLocalDateTime());
                    }

                    // Đồng bộ trạng thái khi load 1 phòng
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();

                    if (room.getActualEndTime() != null && now.isAfter(room.getActualEndTime())) {
                        closeAuctionByTime(roomId);
                        return null;
                    } else if ("SCHEDULED".equalsIgnoreCase(room.getStatus())
                            && room.getStartTime() != null
                            && (now.isEqual(room.getStartTime()) || now.isAfter(room.getStartTime()))) {
                        updateAuctionStatus(roomId, "RUNNING");
                        room.setStatus("RUNNING");
                    }

                    return room;
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi lấy phòng: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy TẤT CẢ các phòng đấu giá (Để Admin quản lý)
     */
    public java.util.List<AuctionRoom> getAllAuctions() {
        java.util.List<AuctionRoom> list = new java.util.ArrayList<>();
        String sql = "SELECT a.auction_id, i.name AS item_name, i.current_price, u.username AS seller_name, a.status " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.item_id " +
                "JOIN users u ON a.seller_id = u.customer_id";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String roomId = rs.getString("auction_id");
                String itemName = rs.getString("item_name");
                double currentPrice = rs.getDouble("current_price");
                String sellerName = rs.getString("seller_name");
                String status = rs.getString("status");

                AuctionRoom room = new AuctionRoom(roomId, itemName, currentPrice, sellerName);

                room.setStatus(status);

                list.add(room);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Admin ép hủy một phiên đấu giá
     */
    public boolean forceDeleteAuction(String roomId) {
        String sql = "UPDATE auctions SET status = 'CANCELED_BY_ADMIN' WHERE auction_id = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            return pstmt.executeUpdate() > 0;

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAuctionStatus(String roomId, String status) {
        String sql = "UPDATE auctions SET status = ? WHERE auction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, roomId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object[] closeAuctionByTime(String roomId) {
        String lockAuctionSql = "SELECT status, seller_id FROM auctions WHERE auction_id = ? FOR UPDATE";
        String getHighestBidSql = "SELECT bidder_id, bid_amount FROM bid_transactions WHERE auction_id = ? ORDER BY bid_amount DESC LIMIT 1";
        String deductBuyerSql = "UPDATE users SET balance = balance - ? WHERE customer_id = ? AND balance >= ?";
        String addSellerSql = "UPDATE users SET balance = balance + ? WHERE customer_id = ?";
        String updateAuctionStatusSql = "UPDATE auctions SET status = ? WHERE auction_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String currentStatus = null;
            String sellerId = null;

            // 1. Khóa phiên để tránh 2 thread cùng chốt
            try (PreparedStatement pstmt = conn.prepareStatement(lockAuctionSql)) {
                pstmt.setString(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new Object[]{false, null, 0.0, null, "Không tìm thấy phiên đấu giá!"};
                    }
                    currentStatus = rs.getString("status");
                    sellerId = rs.getString("seller_id");
                }
            }

            // 2. Nếu phiên đã được xử lý rồi thì bỏ qua
            if ("SOLD".equalsIgnoreCase(currentStatus)
                    || "UNSOLD".equalsIgnoreCase(currentStatus)
                    || "CANCELED_BY_ADMIN".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                return new Object[]{false, null, 0.0, sellerId, "Phiên này đã được xử lý trước đó!"};
            }

            String winnerId = null;
            double finalPrice = 0.0;

            // 3. Tìm giá cao nhất
            try (PreparedStatement pstmt = conn.prepareStatement(getHighestBidSql)) {
                pstmt.setString(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        winnerId = rs.getString("bidder_id");
                        finalPrice = rs.getDouble("bid_amount");
                    }
                }
            }

            String finalStatus = "UNSOLD";

            // 4. Nếu có người thắng thì chuyển tiền
            if (winnerId != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(deductBuyerSql)) {
                    pstmt.setDouble(1, finalPrice);
                    pstmt.setString(2, winnerId);
                    pstmt.setDouble(3, finalPrice);

                    int updatedRows = pstmt.executeUpdate();
                    if (updatedRows == 0) {
                        conn.rollback();
                        return new Object[]{false, winnerId, finalPrice, sellerId, "Người thắng không đủ số dư để thanh toán!"};
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(addSellerSql)) {
                    pstmt.setDouble(1, finalPrice);
                    pstmt.setString(2, sellerId);
                    pstmt.executeUpdate();
                }

                finalStatus = "SOLD";
            }

            // 5. Ghi trạng thái cuối cùng
            try (PreparedStatement pstmt = conn.prepareStatement(updateAuctionStatusSql)) {
                pstmt.setString(1, finalStatus);
                pstmt.setString(2, roomId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return new Object[]{true, winnerId, finalPrice, sellerId, finalStatus};

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return new Object[]{false, null, 0.0, null, "Lỗi Database!"};
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }
}