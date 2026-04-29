package com.auction.client.feature.auth;

/**
 * DTO chứa dữ liệu đặt lại mật khẩu trước khi validate.
 */
public record ResetPasswordForm(String username, String newPassword, String confirmPassword) {}
