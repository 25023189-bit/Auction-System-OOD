package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Truy cập dữ liệu cơ bản cho item được dùng trong phiên đấu giá.
 *
 * Vai trò:
 * - Lưu item mới vào database.
 * - Cập nhật current price của item sau khi có bid hợp lệ.
 *
 * Luồng chính:
 * 1. Nhận Item hoặc itemId/newPrice từ tầng service/DAO khác.
 * 2. Thực thi INSERT/UPDATE qua JDBC và trả về trạng thái thành công.
 *
 * Business rules:
 * - Giá hiện tại ban đầu lấy từ starting price của item.
 * - current_price chỉ nên cập nhật sau khi bid đã qua validate nghiệp vụ.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe theo instance; mỗi method dùng connection local nên không giữ state dùng chung.
 * - Dependency: DatabaseConnection, Item, JDBC, SLF4J.
 */
public class ItemDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemDAO.class);

    public boolean saveItem(Item item) {
        LOGGER.error("Cannot save product without seller_id. Use saveItem(Item, String) with the current schema.");
        return false;
    }

    public boolean saveItem(Item item, String sellerId) {
        String sql = """
                INSERT INTO products (product_name, description, starting_price, current_price, seller_id, product_type)
                VALUES (?, ?, ?, ?, ?, 'ELECTRONICS')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getProductName());
            stmt.setString(2, item.getDescription());
            stmt.setDouble(3, item.getStartingPrice());
            stmt.setDouble(4, item.getStartingPrice());
            stmt.setString(5, sellerId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to save item {}.", item != null ? item.getId() : null, e);
            return false;
        }
    }

    public boolean updateCurrentPrice(String itemId, double newPrice) {
        // Giá hiện tại của item được cập nhật sau mỗi bid hợp lệ.
        String sql = "UPDATE products SET current_price = ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newPrice);
            stmt.setString(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update current price for item {}.", itemId, e);
            return false;
        }
    }
}
