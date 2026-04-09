package com.auction.server.dao;

import com.auction.common.model.Admin;
import com.auction.server.service.FallbackUserStore;
import com.auction.server.utils.DatabaseConnection;
import com.auction.common.model.User;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import java.sql.*;

public class UserDAO {

    // Trong UserDAO.java
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (customer_id, username, password_hash, role, balance) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setDouble(5, 100000);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public User login(String username, String rawPassword) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPass = rs.getString("password_hash");

                    // CHỐNG CRASH MẬT KHẨU CŨ
                    boolean isPasswordMatch = false;
                    try {
                        isPasswordMatch = com.auction.server.utils.PasswordUtil.checkPassword(rawPassword, hashedPass);
                    } catch (IllegalArgumentException e) {
                        // Rơi vào đây tức là tài khoản cũ, mật khẩu chưa được băm
                        isPasswordMatch = rawPassword.equals(hashedPass);
                    }

                    if (isPasswordMatch) {
                        String id = rs.getString("customer_id");
                        String role = rs.getString("role");
                        double balance = rs.getDouble("balance");

                        User user;
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            user = new com.auction.common.model.Admin(id, username, hashedPass, balance);
                        } else if ("SELLER".equalsIgnoreCase(role)) {
                            user = new com.auction.common.model.Seller(id, username, role, hashedPass, balance);
                        } else {
                            user = new com.auction.common.model.Bidder(id, username, role, hashedPass, balance);
                        }

                        user.setRole(role != null ? role.toUpperCase() : "BIDDER");
                        return user;
                    } else {
                        System.out.println("❌ Sai mật khẩu cho user: " + username);
                    }
                } else {
                    System.out.println("❌ Không tìm thấy user: " + username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                    String id = rs.getString("customer_id");
                    String username = rs.getString("username");
                    String hashedPass = rs.getString("password_hash");
                    String role = rs.getString("role");
                    double balance = rs.getDouble("balance");

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        Admin admin = new Admin(id, username, hashedPass, balance);
                        admin.setRole("ADMIN");
                        return admin;
                    } else if ("SELLER".equalsIgnoreCase(role)) {
                        return new Seller(id, username, role, hashedPass, balance);
                    } else {
                        return new Bidder(id, username, role, hashedPass, balance);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ DB getUserById lỗi, thử fallback: " + e.getMessage());
        }

        return FallbackUserStore.getUserById(customerId);
    }

    /**
     * Lấy danh sách toàn bộ người dùng (Trừ ADMIN)
     */
    public java.util.List<User> getAllUsers() {
        java.util.List<User> userList = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role != 'ADMIN'";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("customer_id");
                String username = rs.getString("username");
                String hashedPass = rs.getString("password_hash");
                String role = rs.getString("role");
                double balance = rs.getDouble("balance");

                if ("SELLER".equalsIgnoreCase(role)) {
                    userList.add(new com.auction.common.model.Seller(id, username,role, hashedPass, balance));
                } else {
                    userList.add(new com.auction.common.model.Bidder(id, username,role, hashedPass, balance));
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách User: " + e.getMessage());
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Xóa hoàn toàn một người dùng khỏi hệ thống
     */
    public boolean deleteUser(String customerId) {
        String sql = "DELETE FROM users WHERE customer_id = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            return pstmt.executeUpdate() > 0;

        } catch (java.sql.SQLException e) {
            System.err.println("❌ Lỗi xóa User (Có thể do kẹt khóa ngoại): " + e.getMessage());
            return false;
        }
    }
}