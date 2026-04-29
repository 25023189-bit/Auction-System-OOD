package com.auction.client.feature.auth;

/**
 * DTO nhỏ chứa dữ liệu form đăng nhập trước khi validate.
 */
public record LoginForm(String customerId, String password) {}
