package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;

import java.sql.*;

public class ItemDAO {

    /**
     * Lưu Item vào bảng 'products' và trả về product_id (INT) tự động sinh ra.
     * Trả về -1 nếu lỗi.
     */
    // Trong ItemDAO.java
    public boolean saveItem(Item item) {
        // Khớp với bảng items: item_id, name, description, current_price
        String sql = "INSERT INTO items (item_id, name, description, current_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getCurrentHighestPrice()); // Gán vào current_price
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}