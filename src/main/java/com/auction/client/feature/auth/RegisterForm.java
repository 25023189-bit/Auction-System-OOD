package com.auction.client.feature.auth;

public record RegisterForm(
        String customerId,
        String username,
        String email,         // <-- THÊM MỚI
        String fullName,      // <-- THÊM MỚI
        String password,
        String confirmPassword,
        String role,
        String organization
) {}