package com.auction.server.dao;

import com.auction.common.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface IUserDAO {
    User getUserById(String customerId);

    User getUserByUsername(String username);

    User getUserByUsernameWithEmail(String username);

    User login(String loginIdentifier, String rawPassword);

    String registerUser(User user, String rawPassword);

    boolean resetPassword(String customerId, String newPassword, String confirmPassword);

    String resetPasswordWithNewPassword(String username, String newPassword);

    List<User> getAllUsers();

    boolean deleteUser(String customerId);

    // Map các cột cơ bản của bảng users sang model User.
    default User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("customer_id"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("password_hash"),
                rs.getString("organization"),
                rs.getDouble("balance")
        );
    }

    // Map thêm email/fullName cho luồng quên mật khẩu.
    default User mapUserWithEmail(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("customer_id"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("password_hash"),
                rs.getString("organization"),
                rs.getDouble("balance")
        );
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        return user;
    }

    // customer_id trong hệ thống dùng chữ hoa để tránh sai khác khi query.
    default String normalizeCustomerId(String customerId) {
        return customerId == null ? null : customerId.trim().toUpperCase();
    }

    // Chỉ chấp nhận role nằm trong tập hệ thống hỗ trợ.
    default String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalizedRole = role.trim().toUpperCase();
        return switch (normalizedRole) {
            case "BIDDER", "SELLER", "ADMIN" -> normalizedRole;
            default -> null;
        };
    }

    // Organization chỉ có ý nghĩa với seller.
    default String normalizeOrganization(User user) {
        if (user == null || !"SELLER".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        String organization = user.getOrganization();
        if (organization == null || organization.trim().isEmpty()) {
            return null;
        }
        return organization.trim();
    }

    default String normalizeEmail(User user, String customerId) {
        String email = user != null ? user.getEmail() : null;
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }

        String safeCustomerId = customerId != null && !customerId.isBlank()
                ? customerId.trim().toLowerCase()
                : "user";
        return safeCustomerId + "@auction.local";
    }

    default String normalizeFullName(User user, String username) {
        String fullName = user != null ? user.getFullName() : null;
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        return username != null ? username.trim() : "";
    }

    String generateNextCustomerId();
}
