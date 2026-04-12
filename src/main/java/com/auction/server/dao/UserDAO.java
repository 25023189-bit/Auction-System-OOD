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
     * 2. ĐĂNG KÝ
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

            // TỰ ĐỘNG BẮT ROLE DỰA TRÊN CLASS NẾU BỊ NULL
            String role = user.getRole();
            if (role == null || role.isEmpty()) {
                role = (user instanceof Seller) ? "SELLER" : "BIDDER";
            }
            pstmt.setString(4, role);

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
     * 3. ĐĂNG NHẬP
     */
    public User login(String loginIdentifier, String rawPassword) {
        String sql = "SELECT * FROM users WHERE username = ? OR customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginIdentifier);
            pstmt.setString(2, loginIdentifier);

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
                        String dbCustomerId = rs.getString("customer_id");
                        String dbUsername = rs.getString("username");
                        String dbRole = rs.getString("role");
                        double balance = rs.getDouble("balance");

                        User user = "SELLER".equalsIgnoreCase(dbRole) ?
                                new Seller(dbCustomerId, dbUsername, dbRole, hashedPass, balance) :
                                new Bidder(dbCustomerId, dbUsername, dbRole, hashedPass, balance);

                        System.out.println("✅ Database: Đăng nhập thành công cho User: " + dbUsername + " | Role: " + dbRole);
                        return user;
                    } else {
                        System.out.println("❌ Database: Sai mật khẩu cho tài khoản: " + loginIdentifier);
                    }
                } else {
                    System.out.println("❌ Database: Không tìm thấy tài khoản nào có tên/id là: " + loginIdentifier);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database: Lỗi SQL khi đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 4. QUÊN MẬT KHẨU
     */
    public boolean resetPassword(String loginIdentifier, String token, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ? OR customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedNewPassword = com.auction.server.utils.PasswordUtil.hashPassword(newPassword);
            pstmt.setString(1, hashedNewPassword);
            pstmt.setString(2, loginIdentifier);
            pstmt.setString(3, loginIdentifier);

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

    /**
     * --- HÀM MAIN DÙNG ĐỂ TEST NHANH DATABASE ---
     */
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();
        System.out.println("--- DANH SÁCH TÀI KHOẢN TRONG HỆ THỐNG ---");
        List<User> users = dao.getAllUsers();

        if(users.isEmpty()) {
            System.out.println("Chưa có tài khoản nào trong Database!");
        } else {
            for (User u : users) {
                System.out.println("ID: " + u.getId() +
                        " | Tên: " + u.getUsername() +
                        " | Vai trò: " + u.getRole() +
                        " | Số dư: " + u.getBalance());
            }
        }
    }
}