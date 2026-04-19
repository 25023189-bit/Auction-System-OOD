package com.auction.server.dao;

import com.auction.common.model.User;
import com.auction.server.utils.DatabaseConnection;
import com.auction.server.utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {

    public User getUserById(String customerId) {
        String sql = """
                SELECT customer_id, username, password_hash, role, balance
                FROM users
                WHERE customer_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normalizeCustomerId(customerId));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn User theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String sql = """
                SELECT customer_id, username, password_hash, role, balance
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username != null ? username.trim() : null);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn User theo username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User login(String username, String rawPassword) {
        String sql = """
            SELECT customer_id, username, password_hash, role, balance
            FROM users
            WHERE username = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username != null ? username.trim() : null);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String storedHash = rs.getString("password_hash");
                if (storedHash == null || storedHash.isBlank()) {
                    return null;
                }

                if (!PasswordUtil.checkPassword(rawPassword, storedHash)) {
                    return null;
                }

                return new User(
                        rs.getString("customer_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getDouble("balance")
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng nhập: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String registerUser(User user, String rawPassword) {
        String checkSql = """
                SELECT 1 FROM users
                WHERE customer_id = ? OR username = ?
                """;

        String insertSql = """
                INSERT INTO users (customer_id, username, password_hash, role, balance)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                String customerId = normalizeCustomerId(
                        user.getCustomerId() != null ? user.getCustomerId() : user.getId()
                );

                String username = user.getUsername() != null ? user.getUsername().trim() : null;

                checkStmt.setString(1, customerId);
                checkStmt.setString(2, username);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return "DUPLICATE"; // Báo chính xác là do trùng lặp
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    String passwordHash = PasswordUtil.hashPassword(rawPassword);

                    insertStmt.setString(1, customerId);
                    insertStmt.setString(2, username);
                    insertStmt.setString(3, passwordHash);
                    insertStmt.setString(4, user.getRole());
                    insertStmt.setDouble(5, user.getBalance());

                    return insertStmt.executeUpdate() > 0 ? "SUCCESS" : "FAIL_INSERT";
                }
            }
        }  catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký User mới: " + e.getMessage());
            e.printStackTrace();
            // Trả thẳng cái lỗi của MySQL về để hiện lên màn hình!
            return "LỖI SQL: " + e.getMessage();
        }
    }

    public boolean resetPassword(String customerId, String newPassword, String confirmPassword) {
        if (customerId == null || customerId.isBlank()) return false;
        if (newPassword == null || !newPassword.equals(confirmPassword)) return false;

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE customer_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtil.hashPassword(newPassword));
            stmt.setString(2, normalizeCustomerId(customerId));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = """
                SELECT customer_id, username, password_hash, role, balance
                FROM users
                ORDER BY customer_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(mapUser(rs));
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

            stmt.setString(1, normalizeCustomerId(customerId));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá User: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("customer_id"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("password_hash"),
                rs.getDouble("balance")
        );
    }

    private String normalizeCustomerId(String customerId) {
        return customerId == null ? null : customerId.trim().toUpperCase();
    }

    public String generateNextCustomerId() {
        String sql = """
            SELECT customer_id
            FROM users
            WHERE customer_id LIKE 'BD5%'
            ORDER BY customer_id DESC
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("customer_id");
                String numberPart = lastId.substring(3);
                int nextNumber = Integer.parseInt(numberPart) + 1;
                return "BD5" + String.format("%05d", nextNumber);
            }

            return "BD500001";
        } catch (SQLException e) {
            System.err.println("Lỗi khi sinh customer_id mới: " + e.getMessage());
            e.printStackTrace();
            return "BD500001";
        }
    }
}