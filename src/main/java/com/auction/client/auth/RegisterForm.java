package com.auction.client.auth;

public record RegisterForm(String username, String password, String confirmPassword, String role) {}