package com.auction.client.feature.auth;

public record ResetPasswordForm(String username, String newPassword, String confirmPassword) {
}