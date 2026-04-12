package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import java.sql.*;

public class ItemDAO {

    /**
     * Lưu Item vào Database
     * Nếu DB của bác tên bảng là 'products' thì đổi 'items' thành 'products' nhé!
     */
    public boolean saveItem(Item item) {
        // SQL: item_id (String), name, description, current_price
        String sql = "INSERT INTO items (item_id, name, description, current_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());

            // Dùng getStartingPrice hoặc getCurrentHighestPrice tùy theo model Item của bác
            pstmt.setDouble(4, item.getCurrentHighestPrice());

            int result = pstmt.executeUpdate();
            System.out.println("✅ ItemDAO: Đã lưu sản phẩm thành công: " + item.getId());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi ItemDAO (saveItem): " + e.getMessage());
            // Gợi ý: Nếu báo lỗi 'Table not found', bác hãy đổi 'items' thành 'products' ở câu SQL trên
            return false;
        }
    }

    /**
     * Lấy thông tin Item theo ID (Dùng khi cần xem chi tiết sản phẩm)
     */
    public Item getItemById(String itemId) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                            rs.getString("item_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("current_price")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}