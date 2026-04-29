package com.auction.server.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Generates secure OTP tokens for password reset verification.
 */
public class PasswordResetTokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    private static final int OTP_LENGTH = 6;

    /**
     * Generates a secure random token for email verification.
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generates a 6-digit OTP for email verification.
     */
    public static String generateOTP() {
        return String.format("%06d", RANDOM.nextInt(999999));
    }

    /**
     * Generates a secure temporary password with mixed case, numbers, and special chars.
     */
    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * Creates a PasswordResetToken object with expiration.
     */
    public static PasswordResetToken createToken(String userId, String email, int expirationMinutes) {
        String token = generateToken();
        String otp = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        return new PasswordResetToken(token, otp, userId, email, expiresAt);
    }

    /**
     * Data class for password reset token.
     */
    public static class PasswordResetToken {
        private final String token;
        private final String otp;
        private final String userId;
        private final String email;
        private final LocalDateTime expiresAt;
        private boolean used = false;

        public PasswordResetToken(String token, String otp, String userId, String email, LocalDateTime expiresAt) {
            this.token = token;
            this.otp = otp;
            this.userId = userId;
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        public boolean isValid() {
            return !used && !isExpired();
        }

        // Getters
        public String getToken() {
            return token;
        }

        public String getOtp() {
            return otp;
        }

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public boolean isUsed() {
            return used;
        }

        public void markAsUsed() {
            this.used = true;
        }
    }
}
