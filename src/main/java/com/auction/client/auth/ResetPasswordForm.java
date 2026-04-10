package com.auction.client.auth;

public record ResetPasswordForm(String username, String newPassword, String confirmPassword) {}