package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
// Sửa lỗi: Import đúng vị trí Model User
import com.auction.common.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * Hàm kiểm tra đăng nhập bằng Mã khách hàng (BD5...) và Mật khẩu
     * Theo đặc tả: Hệ thống dùng Mã khách hàng làm định danh duy nhất.
     */
    public User login(String customerId, String password) {
        // Câu lệnh SQL: Tìm user khớp mã khách hàng và mật khẩu
        // Chú ý: Tên cột phải khớp với file database_v5.sql của bạn (customer_id, password_hash)
        String sql = "SELECT * FROM users WHERE customer_id = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Sử dụng Constructor đầy đủ chúng ta vừa thêm vào User.java để code ngắn gọn
                    return new User(
                            rs.getString("customer_id"),
                            rs.getString("username"), // Giả định DB có cột username
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi truy vấn UserDAO (login): " + e.getMessage());
        }
        return null;
    }

    /**
     * Hàm lấy thông tin chi tiết của một User dựa trên ID (BD5xxxxx)
     */
    public User getUserById(String customerId) {
        String sql = "SELECT * FROM users WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("customer_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi getUserById: " + e.getMessage());
        }
        return null;
    }

    /**
     * BỔ SUNG CHO MANAGER: Hàm kiểm tra định dạng mã khách hàng BD5xxxxx
     * Giúp bạn validate dữ liệu trước khi Insert vào Database
     */
    public boolean isValidCustomerId(String id) {
        return id != null && id.matches("^BD5\\d{5}$");
    }
}