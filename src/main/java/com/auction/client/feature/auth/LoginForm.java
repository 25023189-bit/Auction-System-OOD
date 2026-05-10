package com.auction.client.feature.auth;

public record LoginForm(String customerId, String password) {
}

/**
 * DTO chứa dữ liệu form đăng nhập trước khi validate.
 *
 * Vai trò:
 * - Gom customerId/username và password từ UI thành một object.
 * - Là input cho LoginFormValidator.
 *
 * Luồng chính:
 * 1. AuthActionFacade.login tạo LoginForm từ text field/password field.
 * 2. Validator đọc record component để kiểm tra dữ liệu bắt buộc.
 *
 * Business rules:
 * - customerId đại diện cho login identifier người dùng nhập.
 * - DTO không tự trim hoặc validate để validator quyết định lỗi hiển thị.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: record immutable sau khi khởi tạo.
 * - Dependency: AuthActionFacade.login và LoginFormValidator.
 */

