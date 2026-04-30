package com.auction.client.feature.auth;

public record ResetPasswordForm(String username, String newPassword, String confirmPassword) {
}

/**
 * DTO chứa dữ liệu đặt lại mật khẩu trước khi validate.
 */

