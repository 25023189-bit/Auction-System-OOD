package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
import java.sql.*;

public class BidDAO {
    /**
     * Hàm đặt giá: Dùng Transaction để đảm bảo vừa cập nhật giá bảng items,
     * vừa lưu lịch sử vào bảng bid_transactions cùng 1 lúc.
     */
    public boolean placeBid(String roomId, String bidderId, double bidAmount) {
        String checkPriceSql = "SELECT i.current_price, a.item_id FROM auctions a JOIN items i ON a.item_id = i.item_id WHERE a.auction_id = ?";
        String updatePriceSql = "UPDATE items SET current_price = ? WHERE item_id = ?";
        String insertBidSql = "INSERT INTO bid_transactions (auction_id, bidder_id, bid_amount, bid_rank) VALUES (?, ?, ?, 1)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            double currentPrice = 0;
            String itemId = null;

            // 1. Kiểm tra giá hiện tại
            try (PreparedStatement pstmt = conn.prepareStatement(checkPriceSql)) {
                pstmt.setString(1, roomId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    currentPrice = rs.getDouble("current_price");
                    itemId = rs.getString("item_id");
                }
            }

            // Nếu giá nhập vào nhỏ hơn hoặc bằng giá hiện tại -> Từ chối
            if (itemId == null || bidAmount <= currentPrice) {
                conn.rollback();
                return false;
            }

            // 2. Cập nhật giá mới lên bảng items
            try (PreparedStatement pstmt = conn.prepareStatement(updatePriceSql)) {
                pstmt.setDouble(1, bidAmount);
                pstmt.setString(2, itemId);
                pstmt.executeUpdate();
            }

            // 3. Ghi lịch sử đặt giá
            try (PreparedStatement pstmt = conn.prepareStatement(insertBidSql)) {
                pstmt.setString(1, roomId);
                pstmt.setString(2, bidderId);
                pstmt.setDouble(3, bidAmount);
                pstmt.executeUpdate();
            }

            conn.commit(); // Thành công -> Chốt Transaction!
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}