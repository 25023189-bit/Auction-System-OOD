package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
import com.auction.common.model.User;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * 1. HÀM SINH MÃ KHÁCH HÀNG TỰ ĐỘNG
     * (Vẫn giữ lại code ở đây phòng khi sau này nộp bài giáo viên bắt ép dùng chuẩn Đặc tả BD5)
     */
    private String generateNewCustomerId() {
        String sql = "SELECT MAX(customer_id) AS max_id FROM users WHERE customer_id LIKE 'BD5%'";
        String newId = "BD50001"; // Mặc định nếu chưa có ai
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getString("max_id") != null) {
                String lastId = rs.getString("max_id");
                int numberPart = Integer.parseInt(lastId.substring(3));
                newId = String.format("BD5%05d", numberPart + 1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi sinh ID: " + e.getMessage());
        }
        return newId;
    }
    /**
     * 2. ĐĂNG KÝ (Đã fix lỗi Column 'role' cannot be null)
     */
    public String registerUser(User user, String email, String fullName, String rawPassword) {
        String customerId = user.getId();

        String sql = "INSERT INTO users (customer_id, username, password_hash, role, balance) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            pstmt.setString(2, user.getUsername());

            // Mã hóa mật khẩu
            String hashedPass = com.auction.server.utils.PasswordUtil.hashPassword(rawPassword);
            pstmt.setString(3, hashedPass);

            // 🔥 FIX: TỰ ĐỘNG BẮT ROLE DỰA TRÊN CLASS NẾU BỊ NULL
            String role = user.getRole();
            if (role == null || role.isEmpty()) {
                role = (user instanceof Seller) ? "SELLER" : "BIDDER";
            }
            pstmt.setString(4, role); // Đẩy role chuẩn vào Database

            pstmt.setDouble(5, 100000.0); // Khởi tạo 100k

            if (pstmt.executeUpdate() > 0) {
                return customerId;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi Đăng ký (Database Exception): " + e.getMessage());
        }
        return null;
    }

    /**
     * 3. ĐĂNG NHẬP (Bằng Mã Khách Hàng và Role)
     */
    public User login(String customerId, String rawPassword, String requestedRole) {
        String sql = "SELECT * FROM users WHERE customer_id = ? AND role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            pstmt.setString(2, requestedRole);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPass = rs.getString("password_hash");

                    // CHỐNG CRASH MẬT KHẨU CŨ
                    boolean isPasswordMatch = false;
                    try {
                        isPasswordMatch = com.auction.server.utils.PasswordUtil.checkPassword(rawPassword, hashedPass);
                    } catch (IllegalArgumentException e) {
                        isPasswordMatch = rawPassword.equals(hashedPass);
                    }

                    if (isPasswordMatch) {
                        String username = rs.getString("username");
                        double balance = rs.getDouble("balance");

                        User user = "SELLER".equalsIgnoreCase(requestedRole) ?
                                new Seller(customerId, username, requestedRole, hashedPass, balance) :
                                new Bidder(customerId, username, requestedRole, hashedPass, balance);
                        return user;
                    } else {
                        System.out.println("❌ Sai mật khẩu cho ID: " + customerId);
                    }
                } else {
                    System.out.println("❌ Không tìm thấy user hoặc sai vai trò: " + customerId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 4. QUÊN MẬT KHẨU
     */
    public boolean resetPassword(String customerId, String token, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedNewPassword = com.auction.server.utils.PasswordUtil.hashPassword(newPassword);
            pstmt.setString(1, hashedNewPassword);
            pstmt.setString(2, customerId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi resetPassword: " + e.getMessage());
            return false;
        }
    }

    /**
     * 5. LẤY USER THEO ID
     */
    public User getUserById(String customerId) {
        String sql = "SELECT * FROM users WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String hashedPass = rs.getString("password_hash");
                    String role = rs.getString("role");
                    double balance = rs.getDouble("balance");

                    if ("SELLER".equalsIgnoreCase(role)) {
                        return new Seller(customerId, username, role, hashedPass, balance);
                    } else {
                        return new Bidder(customerId, username, role, hashedPass, balance);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi getUserById: " + e.getMessage());
        }
        return null;
    }

    /**
     * 6. LẤY TẤT CẢ USER
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role != 'ADMIN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("customer_id");
                String username = rs.getString("username");
                String hashedPass = rs.getString("password_hash");
                String role = rs.getString("role");
                double balance = rs.getDouble("balance");

                if ("SELLER".equalsIgnoreCase(role)) {
                    userList.add(new Seller(id, username, role, hashedPass, balance));
                } else {
                    userList.add(new Bidder(id, username, role, hashedPass, balance));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi getAllUsers: " + e.getMessage());
        }
        return userList;
    }

    /**
     * 7. XÓA USER
     */
    public boolean deleteUser(String customerId) {
        String sql = "DELETE FROM users WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa User: " + e.getMessage());
            return false;
        }
    }
}