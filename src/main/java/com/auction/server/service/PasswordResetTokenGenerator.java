package com.auction.server.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Tiện ích sinh token, OTP và mật khẩu tạm cho luồng reset password.
 *
 * Vai trò:
 * - Tạo token URL-safe, OTP 6 chữ số và mật khẩu tạm ngẫu nhiên.
 * - Tạo PasswordResetToken chứa thông tin user/email và thời điểm hết hạn.
 *
 * Luồng chính:
 * 1. ForgotPasswordService hoặc luồng OTP gọi static generator tương ứng.
 * 2. Generator dùng SecureRandom để tạo dữ liệu rồi trả chuỗi hoặc token object.
 *
 * Business rules:
 * - OTP luôn có 6 chữ số, có padding 0 khi cần.
 * - Mật khẩu tạm dài 12 ký tự và lấy từ tập chữ hoa, chữ thường, số, ký tự đặc biệt.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: SecureRandom dùng chung an toàn cho các lời gọi đồng thời.
 * - Dependency: SecureRandom, Base64, LocalDateTime.
 */
public class PasswordResetTokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    private static final int OTP_LENGTH = 6;

    /**
     * Sinh token ngẫu nhiên dùng trong email verification.
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Sinh OTP 6 chữ số.
     */
    public static String generateOTP() {
        return String.format("%06d", RANDOM.nextInt(999999));
    }

    /**
     * Sinh mật khẩu tạm có chữ hoa, chữ thường, số và ký tự đặc biệt.
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
     * Tạo token reset password kèm thời điểm hết hạn.
     */
    public static PasswordResetToken createToken(String userId, String email, int expirationMinutes) {
        String token = generateToken();
        String otp = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        return new PasswordResetToken(token, otp, userId, email, expiresAt);
    }

    /**
     * Model lưu token reset password trong bộ nhớ.
     *
     * Vai trò:
     * - Giữ token, OTP, userId, email, thời điểm hết hạn và trạng thái đã dùng.
     * - Cung cấp helper kiểm tra token còn valid hay không.
     *
     * Luồng chính:
     * 1. PasswordResetTokenGenerator.createToken() tạo object và service lưu vào tokenStore.
     * 2. ForgotPasswordService đọc object để verify OTP, đánh dấu used hoặc xóa khi hết hạn.
     *
     * Business rules:
     * - Token hết hạn khi thời gian hiện tại sau expiresAt.
     * - Token đã markAsUsed() không còn hợp lệ dù chưa hết hạn.
     *
     * Ghi chú kỹ thuật:
     * - Không thread-safe đầy đủ: field used mutable, caller cần quản lý truy cập nếu dùng cùng token đồng thời.
     * - Dependency: LocalDateTime và PasswordResetTokenGenerator.
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

        // Getter phục vụ ForgotPasswordService kiểm tra trạng thái token.
        public String getToken() { return token; }
        public String getOtp() { return otp; }
        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public boolean isUsed() { return used; }
        public void markAsUsed() { this.used = true; }
    }
}
