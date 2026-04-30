package com.auction.server.service;

/**
 * Kiểm tra độ mạnh mật khẩu theo các tiêu chí bảo mật cơ bản.
 */
public class PasswordStrengthValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MIN_UPPERCASE = 1;
    private static final int MIN_LOWERCASE = 1;
    private static final int MIN_DIGITS = 1;
    private static final int MIN_SPECIAL = 1;

    // Lưu lỗi cuối cùng để handler trả lý do cụ thể cho client.
    private String lastError = "";

    public boolean isStrong(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            lastError = "Password must be at least " + MIN_LENGTH + " characters long";
            return false;
        }

        // Đếm từng nhóm ký tự để báo lỗi chính xác tiêu chí còn thiếu.
        int uppercaseCount = 0, lowercaseCount = 0, digitCount = 0, specialCount = 0;
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) uppercaseCount++;
            else if (Character.isLowerCase(c)) lowercaseCount++;
            else if (Character.isDigit(c)) digitCount++;
            else if (specialChars.indexOf(c) >= 0) specialCount++;
        }

        if (uppercaseCount < MIN_UPPERCASE) {
            lastError = "Password must contain at least " + MIN_UPPERCASE + " uppercase letter";
            return false;
        }
        if (lowercaseCount < MIN_LOWERCASE) {
            lastError = "Password must contain at least " + MIN_LOWERCASE + " lowercase letter";
            return false;
        }
        if (digitCount < MIN_DIGITS) {
            lastError = "Password must contain at least " + MIN_DIGITS + " digit";
            return false;
        }
        if (specialCount < MIN_SPECIAL) {
            lastError = "Password must contain at least " + MIN_SPECIAL + " special character";
            return false;
        }

        return true;
    }

    public String getLastError() {
        return lastError;
    }
}
