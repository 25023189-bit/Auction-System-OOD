package com.auction.client.feature.auth;

public record RegisterForm(
        String username,
        String password,
        String confirmPassword,
        String role,
        String organization
) {}
