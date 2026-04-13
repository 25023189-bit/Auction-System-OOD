package com.auction.server.dao;

import com.auction.common.model.User;
import com.auction.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ==========================================
    // CÁC HÀM CŨ CỦA ANH (ĐÃ GIỮ NGUYÊN)
    // ==========================================

    public User getUserById(String customerId) {
        String sql = "SELECT * FROM users WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // DB mới không có username hay balance -> Lấy CustomerID làm tên, tiền = 0
                return new User(
                        rs.getString("customer_id"),
                        rs.getString("customer_id"), // Lấy ID làm Username luôn
                        rs.getString("role"),
                        rs.getString("password"),
                        0.0 // Balance mặc định 0.0
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn User theo ID: " + e.getMessage());
        }
        return null;
    }

    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (customer_id, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getCustomerId());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm User mới: " + e.getMessage());
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(new User(
                        rs.getString("customer_id"),
                        rs.getString("customer_id"),
                        rs.getString("role"),
                        rs.getString("password"),
                        0.0
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public boolean deleteUser(String customerId) {
        String sql = "DELETE FROM users WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá User: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 3 HÀM MỚI BỔ SUNG ĐỂ FIX LỖI Ở AuthService
    // ==========================================

    public User login(String customerId, String password) {
        String sql = "SELECT * FROM users WHERE customer_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("customer_id"),
                        rs.getString("customer_id"),
                        rs.getString("role"),
                        rs.getString("password"),
                        0.0
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng nhập: " + e.getMessage());
        }
        return null; // Trả về null nếu sai thông tin
    }

    public boolean registerUser(User user, String rawPassword) {
        String sql = "INSERT INTO users (customer_id, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getCustomerId());
            // Ưu tiên dùng rawPassword nếu có truyền vào, không thì lấy password từ object User
            stmt.setString(2, rawPassword != null ? rawPassword : user.getPassword());
            stmt.setString(3, user.getRole());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký User mới: " + e.getMessage());
        }
        return false;
    }

    public boolean resetPassword(String customerId, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE customer_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, customerId);
            stmt.setString(3, oldPassword);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
        }
        return false;
    }
}