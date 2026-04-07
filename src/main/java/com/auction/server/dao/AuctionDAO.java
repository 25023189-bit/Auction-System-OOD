package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionDAO {

    // Trong AuctionDAO.java
    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        // Khớp với bảng auctions: auction_id, item_id, seller_id, status
        String sql = "INSERT INTO auctions (auction_id, item_id, seller_id, status) VALUES (?, ?, ?, 'RUNNING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomId());
            pstmt.setString(2, itemId);
            pstmt.setString(3, sellerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public java.util.List<AuctionRoom> getAllActiveAuctions() {
        java.util.List<AuctionRoom> list = new java.util.ArrayList<>();

        // Nối 3 bảng lại để lấy đầy đủ thông tin hiển thị lên giao diện
        String sql = "SELECT a.auction_id, i.name AS item_name, i.current_price, u.username AS seller_name " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.item_id " +
                "JOIN users u ON a.seller_id = u.customer_id " +
                "WHERE a.status = 'RUNNING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String roomId = rs.getString("auction_id");
                String itemName = rs.getString("item_name");
                double currentPrice = rs.getDouble("current_price");
                String sellerName = rs.getString("seller_name");

                list.add(new AuctionRoom(roomId, itemName, currentPrice, sellerName));
            }
        } catch (SQLException e) {
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
        String sql = "SELECT a.auction_id, i.name AS item_name, i.current_price, u.username AS seller_name " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.item_id " +
                "JOIN users u ON a.seller_id = u.customer_id " +
                "WHERE a.auction_id = ?";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new AuctionRoom(
                            rs.getString("auction_id"),
                            rs.getString("item_name"),
                            rs.getDouble("current_price"),
                            rs.getString("seller_name")
                    );
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi lấy phòng: " + e.getMessage());
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
}