package com.auction.server.dao;

import com.auction.common.model.User;
import com.auction.server.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thi các thao tác truy cập dữ liệu liên quan đến User.
 * Triển khai interface IUserDAO để phục vụ Mocking và Dependency Injection.
 */
public class UserDAO implements IUserDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    @Override
    public User getUserById(String customerId) {
        String sql = "SELECT * FROM users WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi lấy user bằng ID: {}", customerId, e);
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi lấy user bằng username: {}", username, e);
        }
        return null;
    }

    @Override
    public User login(String loginIdentifier, String rawPassword) {
        // Chấp nhận đăng nhập bằng cả username hoặc customer_id
        String sql = "SELECT * FROM users WHERE username = ? OR customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loginIdentifier);
            stmt.setString(2, loginIdentifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    // Kiểm tra mật khẩu mã hóa bằng BCrypt
                    if (user != null && BCrypt.checkpw(rawPassword, rs.getString("password_hash"))) {
                        return user;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi trong quá trình xử lý đăng nhập cho: {}", loginIdentifier, e);
        }
        return null;
    }

    @Override
    public String registerUser(User user, String rawPassword) {
        if (getUserByUsername(user.getUsername()) != null) {
            return "DUPLICATE";
        }

        String sql = """
                INSERT INTO users (customer_id, username, password_hash, full_name, role, organization, balance)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String nextId = user.getId() != null ? user.getId() : generateNextCustomerId();
            String hashedPass = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            stmt.setString(1, nextId);
            stmt.setString(2, user.getUsername());
            stmt.setString(3, hashedPass);
            stmt.setString(4, resolveFullName(user));
            stmt.setString(5, user.getRole() != null ? user.getRole().toUpperCase() : "BIDDER");
            stmt.setString(6, user.getOrganization());
            stmt.setDouble(7, user.getBalance());

            if (stmt.executeUpdate() > 0) {
                user.setCustomerId(nextId);
                return "SUCCESS";
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi ghi nhận đăng ký user mới: {}", user.getUsername(), e);
            return e.getMessage();
        }
        return "FAILED";
    }

    @Override
    public boolean resetPassword(String customerId, String newPassword, String confirmPassword) {
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            return false;
        }
        String hashedPass = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPass);
            stmt.setString(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi đổi mật khẩu cho user ID: {}", customerId, e);
            return false;
        }
    }

    @Override
    public String resetPasswordWithNewPassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        if (user == null) return "USER_NOT_FOUND";

        String hashedPass = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPass);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0 ? "SUCCESS" : "FAILED";
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi reset mật khẩu cho username: {}", username, e);
            return "ERROR";
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi lấy danh sách toàn bộ user", e);
        }
        return list;
    }

    @Override
    public boolean deleteUser(String customerId) {
        String sql = "DELETE FROM users WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi xóa user khỏi hệ thống. ID: {}", customerId, e);
            return false;
        }
    }

    @Override
    public String generateNextCustomerId() {
        String sql = "SELECT customer_id FROM users ORDER BY customer_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("customer_id");
                if (lastId != null && lastId.startsWith("U")) {
                    int num = Integer.parseInt(lastId.substring(1));
                    return String.format("U%03d", num + 1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi khi tự động sinh mã ID khách hàng tiếp theo", e);
        }
        return "U001";
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setCustomerId(rs.getString("customer_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setOrganization(rs.getString("organization"));
        user.setBalance(rs.getDouble("balance"));
        return user;
    }

    private String resolveFullName(User user) {
        String fullName = user.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName.trim();
        }
        return user.getUsername();
    }
}
