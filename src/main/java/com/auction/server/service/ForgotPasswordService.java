package com.auction.server.service;

import com.auction.common.model.User;
import com.auction.config.ConfigManager;
import com.auction.server.dao.UserDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Forgot Password Service with rate limiting and audit logging.
 * Modified to generate a new password and send directly to email.
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
     * Main method to process forgot password request.
     * Generates a new password directly and sends it to the user's email.
     */
    public String processForgotPassword(String username) {
        if (username == null || username.trim().isEmpty()) {
            AuditLogger.logPasswordResetFailure(username, "Empty username");
            return "INVALID_USERNAME";
        }

        username = username.trim();

        // Check rate limiting
        if (!rateLimiter.isAllowed(username)) {
            AuditLogger.logPasswordResetFailure(username, "Rate limit exceeded");
            return "RATE_LIMITED";
        }

        AuditLogger.logPasswordResetRequested(username, "***@***");

        // 1. Find user in database
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

        // 2. Generate a new temporary password directly
        String tempPassword = PasswordResetTokenGenerator.generateTemporaryPassword();

        // 3. Update the new password in the database
        String updateResult = userDAO.resetPasswordWithNewPassword(user.getId(), tempPassword);
        if (!"SUCCESS".equals(updateResult)) {
            AuditLogger.logPasswordResetFailure(user.getUsername(), "Database update failed");
            return "DB_ERROR";
        }

        // 4. Send the new password to the user's email
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
     * Verifies OTP (Kept for compatibility if you ever decide to add OTP screen back)
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
     * Validates and updates password after user changes it from temporary password.
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
     * Gets remaining password reset requests for a user in current window.
     */
    public int getRemainingRequests(String username) {
        return rateLimiter.getRemainingRequests(username);
    }

    /**
     * Cleans up expired tokens (should be called periodically).
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        System.out.println("🧹 Cleanup: Removed expired password reset tokens");
    }

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