package com.auction.server.service;

import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;

public class ForgotPasswordService {
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();

    public String processForgotPassword(String username) {
        // 1. Tìm user bằng username/customerId
        User user = userDAO.getUserByUsernameWithEmail(username);

        if (user == null) {
            System.out.println("❌ User not found: " + username);
            return "USER_NOT_FOUND";
        }

        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ User has no email on file: " + username);
            return "NO_EMAIL";
        }

        // 2. Tạo mật khẩu ngẫu nhiên
        String newPassword = generateRandomPassword();
        System.out.println("[ForgotPasswordService] Generated temporary password for: " + username);

        // 3. Lưu mật khẩu mới vào DB
        String updateResult = userDAO.resetPasswordWithNewPassword(username, newPassword);
        if (!"SUCCESS".equals(updateResult)) {
            System.out.println("❌ Database update failed: " + updateResult);
            return "DB_ERROR";
        }

        // 4. Gửi email với mật khẩu mới
        boolean emailSent = emailService.sendPasswordResetEmail(
                email.trim(),
                user.getUsername(),
                newPassword
        );

        return emailSent ? "SUCCESS" : "EMAIL_FAILED";
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }
}
