package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.utils.PasswordUtil;

/**
 * Lớp AuthService đóng vai trò là Tầng Nghiệp Vụ (Business Logic Layer).
 * Xử lý mọi thao tác liên quan đến định danh người dùng: Đăng nhập, Đăng ký, Đổi mật khẩu.
*/

public class AuthService {
    private UserDAO userDAO = new UserDAO();

    public Message login(String username, String password) {
        User user = null;

        try {
            user = userDAO.login(username, password);
            System.out.println("DB login result = " + (user == null ? "null" : user.getUsername() + " | " + user.getRole()));
        } catch (Exception e) {
            System.out.println("⚠️ DB login lỗi, chuyển sang fallback: " + e.getMessage());
        }

        if (user == null) {
            user = FallbackUserStore.login(username, password);
            System.out.println("Fallback login result = " + (user == null ? "null" : user.getUsername() + " | " + user.getRole()));
        }

        if (user != null) {
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        }

        return new Message("LOGIN_FAIL", "SERVER", "Sai tài khoản hoặc mật khẩu!");
    }

    public Message register(String username, String password, String role) {
        System.out.println("Người dùng đã đăng ký: " + username);

        String finalRole = "BIDDER";
        if (role != null && role.toUpperCase().contains("SELLER")) {
            finalRole = "SELLER";
        }

        String newId = "BD5" + (System.currentTimeMillis() % 100000);
        String hashedPass = com.auction.server.utils.PasswordUtil.hashPassword(password);

        User newUser;
        if (finalRole.equals("SELLER")) {
            newUser = new com.auction.common.model.Seller(newId, username, hashedPass, 0.0);
        } else {
            newUser = new com.auction.common.model.Bidder(newId, username, hashedPass, 0.0);
        }
        newUser.setRole(finalRole);

        try {
            if (userDAO.registerUser(newUser)) {
                return new Message("REGISTER_SUCCESS", "SERVER", newId);
            }
        } catch (Exception e) {
            System.out.println("⚠️ DB register lỗi, chuyển sang fallback: " + e.getMessage());
        }

        boolean fallbackOk = FallbackUserStore.register(username, password, finalRole);
        if (fallbackOk) {
            return new Message("REGISTER_SUCCESS", "SERVER", newId);
        }

        return new Message("REGISTER_FAIL", "SERVER", "Tên tài khoản đã tồn tại!");
    }

    public Message resetPassword(String username, String newPassword) {
        if (newPassword == null || newPassword.length() < 6 || newPassword.contains(" ")) {
            return new Message("RESET_FAIL", "SERVER", "Mật khẩu quá yếu (Cần >= 6 ký tự và không có khoảng trắng)!");
        }

        String hashedNewPassword = PasswordUtil.hashPassword(newPassword);

        try {
            boolean isUpdated = userDAO.updatePassword(username, hashedNewPassword);
            if (isUpdated) {
                System.out.println("🔄 " + username + " vừa đổi mật khẩu mới trong Database.");
                return new Message("RESET_SUCCESS", "SERVER", "Đổi mật khẩu thành công!");
            }
        } catch (Exception e) {
            System.out.println("⚠️ DB reset password lỗi, chuyển sang fallback: " + e.getMessage());
        }

        boolean fallbackUpdated = FallbackUserStore.resetPassword(username, newPassword);
        if (fallbackUpdated) {
            return new Message("RESET_SUCCESS", "SERVER", "Đổi mật khẩu thành công!");
        }

        return new Message("RESET_FAIL", "SERVER", "Lỗi: Không tìm thấy tên đăng nhập này trong hệ thống!");
    }
}