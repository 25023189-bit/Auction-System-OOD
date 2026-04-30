package com.auction.client.feature.auth;

public record LoginForm(String customerId, String password) {
}

/**
 * DTO nhỏ chứa dữ liệu form đăng nhập trước khi validate.
 */

