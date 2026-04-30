package com.auction.server.service;

import com.auction.common.model.User;
import com.auction.config.ConfigManager;
import com.auction.server.dao.UserDAO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service xử lý quên mật khẩu.
 * Kiểm tra rate limit, tạo mật khẩu tạm, cập nhật DB và gửi email thông báo.
 */
public class ForgotPasswordService {
    private final UserDAO userDAO;
    private final EmailService emailService;
    private final PasswordStrengthValidator passwordValidator;
    private final RateLimiter rateLimiter;
    private final ConcurrentHashMap<String, PasswordResetTokenGenerator.PasswordResetToken> tokenStore;

    public ForgotPasswordService() {
        this.userDAO = new UserDAO();
        this.emailService = new EmailService();
        this.passwordValidator = new PasswordStrengthValidator();
        this.rateLimiter = new RateLimiter();
        this.tokenStore = new ConcurrentHashMap<>();
    }

    /**
     * Xử lý yêu cầu quên mật khẩu và trả về mã trạng thái cho AuthActionHandler.
     */
    public String processForgotPassword(String username) {
        if (username == null || username.trim().isEmpty()) {
            AuditLogger.logPasswordResetFailure(username, "Empty username");
            return "INVALID_USERNAME";
        }

        username = username.trim();

        // Chặn spam reset password theo từng username.
        if (!rateLimiter.isAllowed(username)) {
            AuditLogger.logPasswordResetFailure(username, "Rate limit exceeded");
            return "RATE_LIMITED";
        }

        AuditLogger.logPasswordResetRequested(username, "***@***");

        // 1. Tìm user và email đã đăng ký.
        User user = userDAO.getUserByUsernameWithEmail(username);
        if (user == null) {
            AuditLogger.logPasswordResetFailure(username, "User not found");
            return "USER_NOT_FOUND";
        }

        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            AuditLogger.logPasswordResetFailure(user.getUsername(), "No email on file");
            return "NO_EMAIL";
        }

        // 2. Sinh mật khẩu tạm thời đủ mạnh.
        String tempPassword = PasswordResetTokenGenerator.generateTemporaryPassword();

        // 3. Lưu hash mật khẩu mới vào database trước khi gửi email.
        String updateResult = userDAO.resetPasswordWithNewPassword(user.getId(), tempPassword);
        if (!"SUCCESS".equals(updateResult)) {
            AuditLogger.logPasswordResetFailure(user.getUsername(), "Database update failed");
            return "DB_ERROR";
        }

        // 4. Gửi mật khẩu tạm tới email của user.
        boolean emailSent = emailService.sendPasswordResetEmail(
                email.trim(),
                user.getUsername(),
                tempPassword
        );

        if (!emailSent) {
            AuditLogger.logPasswordResetFailure(user.getUsername(), "Email send failed");
            return "EMAIL_FAILED";
        }

        AuditLogger.logPasswordResetSuccess(user.getId());
        System.out.println("✅ New password sent to " + maskEmail(email) + " for user: " + user.getUsername());
        return "SUCCESS";
    }

    /**
     * Xác thực OTP, hiện giữ để tương thích nếu bật lại màn hình OTP.
     */
    public String verifyOTPAndGetTemporaryPassword(String tokenId, String otp) {
        PasswordResetTokenGenerator.PasswordResetToken token = tokenStore.get(tokenId);

        if (token == null) {
            AuditLogger.logInvalidTokenAttempt("unknown", tokenId);
            return "INVALID_TOKEN";
        }

        if (token.isExpired()) {
            AuditLogger.logInvalidTokenAttempt(token.getUserId(), tokenId);
            tokenStore.remove(tokenId);
            return "EXPIRED_TOKEN";
        }

        if (token.isUsed()) {
            AuditLogger.logInvalidTokenAttempt(token.getUserId(), tokenId);
            return "TOKEN_ALREADY_USED";
        }

        if (!token.getOtp().equals(otp)) {
            AuditLogger.logInvalidTokenAttempt(token.getUserId(), tokenId);
            return "INVALID_OTP";
        }

        String tempPassword = PasswordResetTokenGenerator.generateTemporaryPassword();

        String updateResult = userDAO.resetPasswordWithNewPassword(token.getUserId(), tempPassword);
        if (!"SUCCESS".equals(updateResult)) {
            AuditLogger.logPasswordResetFailure(token.getUserId(), "Database update failed");
            return "DB_ERROR";
        }

        User user = userDAO.getUserById(token.getUserId());
        if (user != null) {
            emailService.sendPasswordResetEmail(
                    token.getEmail(),
                    user.getUsername(),
                    tempPassword
            );
        }

        token.markAsUsed();
        AuditLogger.logPasswordResetSuccess(token.getUserId());
        return "SUCCESS|" + tempPassword;
    }

    /**
     * Validate và cập nhật mật khẩu mới sau khi user đổi từ mật khẩu tạm.
     */
    public String validateAndUpdatePassword(String userId, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return "PASSWORDS_DONT_MATCH";
        }

        if (!passwordValidator.isStrong(newPassword)) {
            return "WEAK_PASSWORD|" + passwordValidator.getLastError();
        }

        String result = userDAO.resetPasswordWithNewPassword(userId, newPassword);
        if ("SUCCESS".equals(result)) {
            AuditLogger.logPasswordChanged(userId, "User-initiated password change");
            User user = userDAO.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                emailService.sendPasswordChangedConfirmation(
                        user.getEmail(),
                        user.getUsername(),
                        now.format(formatter)
                );
            }
        }

        return result;
    }

    /**
     * Trả về số lượt reset còn lại trong cửa sổ rate limit hiện tại.
     */
    public int getRemainingRequests(String username) {
        return rateLimiter.getRemainingRequests(username);
    }

    /**
     * Dọn token OTP hết hạn nếu luồng OTP được sử dụng.
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        System.out.println("🧹 Cleanup: Removed expired password reset tokens");
    }

    // Che bớt email khi ghi log để tránh lộ thông tin cá nhân.
    private String maskEmail(String email) {
        if (email == null || email.length() < 5) return "***@***";
        String[] parts = email.split("@");
        if (parts.length != 2) return "***@***";

        String name = parts[0];
        String domain = parts[1];
        String maskedName = name.charAt(0) + "*".repeat(Math.max(0, name.length() - 2)) + (name.length() > 1 ? name.charAt(name.length() - 1) : "");
        String maskedDomain = domain.substring(0, Math.min(2, domain.length())) + "***";

        return maskedName + "@" + maskedDomain;
    }
}
