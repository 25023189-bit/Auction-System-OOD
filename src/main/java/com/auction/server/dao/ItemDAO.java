package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ItemDAO {

    public boolean saveItem(Item item) {
        String sql = """
                INSERT INTO items (item_id, name, description, current_price)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getId());
            stmt.setString(2, item.getProductName());
            stmt.setString(3, item.getDescription());
            stmt.setDouble(4, item.getStartingPrice());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu Item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCurrentPrice(String itemId, double newPrice) {
        String sql = "UPDATE items SET current_price = ? WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newPrice);
            stmt.setString(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật current_price của item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}