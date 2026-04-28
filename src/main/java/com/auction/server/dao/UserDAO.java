package com.auction.server.dao;

import com.auction.common.model.User;
import com.auction.server.utils.DatabaseConnection;
import com.auction.server.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    public User getUserById(String customerId) {
        String sql = """
                SELECT customer_id, username, password_hash, role, organization, balance
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
            LOGGER.error("Failed to query user by customer id {}.", customerId, e);
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String sql = """
                SELECT customer_id, username, password_hash, role, organization, balance
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
            LOGGER.error("Failed to query user by username {}.", username, e);
        }
        return null;
    }

    public User login(String loginIdentifier, String rawPassword) {
        // Sửa câu SQL: Tìm kiếm theo username HOẶC customer_id
        String sql = """
            SELECT customer_id, username, password_hash, role, organization, balance
            FROM users
            WHERE username = ? OR customer_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String safeInput = loginIdentifier != null ? loginIdentifier.trim() : null;
            // Set cùng 1 giá trị đầu vào cho cả 2 dấu chấm hỏi (?)
            stmt.setString(1, safeInput);
            stmt.setString(2, safeInput);

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

                return mapUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to login user {}.", loginIdentifier, e);
            return null;
        }
    }
    public String registerUser(User user, String rawPassword) {
        String checkSql = """
                SELECT 1 FROM users
                WHERE customer_id = ? OR username = ? OR email = ?
                """; // Thêm check trùng email luôn cho xịn

        String insertSql = """
                INSERT INTO users (customer_id, username, password_hash, role, organization, balance, email, full_name)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                String customerId = normalizeCustomerId(user.getCustomerId() != null ? user.getCustomerId() : user.getId());
                String username = user.getUsername() != null ? user.getUsername().trim() : null;
                String email = user.getEmail() != null ? user.getEmail().trim() : ""; // Lấy Email thật
                String fullName = user.getFullName() != null ? user.getFullName().trim() : ""; // Lấy Họ Tên thật

                String role = normalizeRole(user.getRole());

                if (role == null) {
                    return "INVALID_ROLE";
                }

                checkStmt.setString(1, customerId);
                checkStmt.setString(2, username);
                checkStmt.setString(3, email);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return "DUPLICATE";
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    String passwordHash = PasswordUtil.hashPassword(rawPassword);

                    insertStmt.setString(1, customerId);
                    insertStmt.setString(2, username);
                    insertStmt.setString(3, passwordHash);
                    insertStmt.setString(4, role);
                    insertStmt.setString(5, normalizeOrganization(user));
                    insertStmt.setDouble(6, user.getBalance());

                    // Truyền data thật xuống DB
                    insertStmt.setString(7, email);
                    insertStmt.setString(8, fullName);

                    return insertStmt.executeUpdate() > 0 ? "SUCCESS" : "FAIL_INSERT";
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to register user {}.", user != null ? user.getUsername() : null, e);
            return "SQL ERROR: " + e.getMessage();
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
            LOGGER.error("Failed to reset password for customer id {}.", customerId, e);
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = """
                SELECT customer_id, username, password_hash, role, organization, balance
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
            LOGGER.error("Failed to load all users.", e);
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
            LOGGER.error("Failed to delete user {}.", customerId, e);
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("customer_id"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("password_hash"),
                rs.getString("organization"),
                rs.getDouble("balance")
        );
    }

    private String normalizeCustomerId(String customerId) {
        return customerId == null ? null : customerId.trim().toUpperCase();
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalizedRole = role.trim().toUpperCase();
        return switch (normalizedRole) {
            case "BIDDER", "SELLER", "ADMIN" -> normalizedRole;
            default -> null;
        };
    }

    private String normalizeOrganization(User user) {
        if (user == null || !"SELLER".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        String organization = user.getOrganization();
        if (organization == null || organization.trim().isEmpty()) {
            return null;
        }
        return organization.trim();
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
            LOGGER.error("Failed to generate next customer id.", e);
            return "BD500001";
        }
    }
}

