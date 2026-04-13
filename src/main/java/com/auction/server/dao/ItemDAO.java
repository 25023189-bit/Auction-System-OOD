package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemDAO {

    // Đã đổi từ insertItem(Item) -> saveItem(Item) và trả về boolean
    public boolean saveItem(Item item) {
        String sql = "INSERT INTO products (product_name, description, starting_price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getProductName());
            stmt.setString(2, item.getDescription());
            stmt.setDouble(3, item.getStartingPrice());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu Sản phẩm (saveItem): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}