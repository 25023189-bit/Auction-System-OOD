package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
import com.auction.common.model.User;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import java.sql.*;

public class UserDAO {

    // Trong UserDAO.java
    public boolean registerUser(User user) {
        // Khớp với bảng users: customer_id, username, password_hash, role, balance
        String sql = "INSERT INTO users (customer_id, username, password_hash, role, balance) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword()); // Đây là password_hash
            pstmt.setString(4, user.getRole());     // ENUM khớp với 'BIDDER'/'SELLER'
            pstmt.setDouble(5, 100000);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public User login(String username, String rawPassword) {
        // 1. Chỉ tìm User bằng username (hoặc customer_id tùy logic của bạn)
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 2. Lấy cái cục mật khẩu đã bị băm từ Database lên
                    String hashedPassInDB = rs.getString("password_hash");

                    // 3. Dùng PasswordUtil để đọ xem mật khẩu nhập vào có khớp không
                    if (com.auction.server.utils.PasswordUtil.checkPassword(rawPassword, hashedPassInDB)) {

                        String role = rs.getString("role");
                        String id = rs.getString("customer_id");
                        double balance = rs.getDouble("balance"); // Nếu DB bạn ko có cột balance thì tạm xóa dòng này

                        if ("SELLER".equalsIgnoreCase(role)) {
                            return new com.auction.common.model.Seller(id, username,hashedPassInDB , balance);
                        } else if ("ADMIN".equalsIgnoreCase(role)) {
                            return new com.auction.common.model.Admin(id, username, hashedPassInDB, balance);
                        } else {
                            return new com.auction.common.model.Bidder(id, username, hashedPassInDB, balance);
                        }
                    } else {
                        System.out.println("❌ Mật khẩu không khớp!");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi truy vấn UserDAO (login): " + e.getMessage());
        }
        return null;
    }

    /**
     * Cập nhật mật khẩu mới vào Database
     */
    public boolean updatePassword(String username, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, username);

            // Nếu executeUpdate() trả về > 0 nghĩa là đã có dòng được cập nhật thành công
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi updatePassword: " + e.getMessage());
            return false;
        }
    }

    public User getUserById(String customerId) {
        String sql = "SELECT * FROM users WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Lấy các thông tin cần thiết từ Database
                    String id = rs.getString("customer_id");
                    String username = rs.getString("username");
                    String hashedPass = rs.getString("password_hash");
                    String role = rs.getString("role");
                    double balance = rs.getDouble("balance");

                    // 🌟 QUAN TRỌNG: Khởi tạo đúng Class dựa vào Role để không bị lỗi ép kiểu sau này
                    if ("SELLER".equalsIgnoreCase(role)) {
                        return new com.auction.common.model.Seller(id, username, hashedPass, balance);
                    } else {
                        return new com.auction.common.model.Bidder(id, username, hashedPass, balance);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi truy vấn UserDAO (getUserById): " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Trả về null nếu không tìm thấy ai có ID này
    }
}