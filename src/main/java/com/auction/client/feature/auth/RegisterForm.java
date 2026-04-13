package com.auction.client.feature.auth;

public record RegisterForm(
        String customerId,
        String password,
        String confirmPassword,
        String role
) {}