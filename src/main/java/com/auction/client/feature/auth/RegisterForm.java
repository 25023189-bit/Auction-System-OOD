package com.auction.client.feature.auth;

/**
 * DTO chứa dữ liệu đăng ký lấy từ FXML.
 *
 * Vai trò:
 * - Gom thông tin tài khoản, hồ sơ và role trước khi validate/register.
 * - Truyền organization cho seller để phục vụ xét duyệt tạo phiên sau này.
 *
 * Luồng chính:
 * 1. AuctionController đọc các field đăng ký và tạo RegisterForm.
 * 2. RegisterFormValidator kiểm tra dữ liệu, RegisterCommand gửi dữ liệu hợp lệ lên server.
 *
 * Business rules:
 * - password và confirmPassword phải khớp ở bước validate.
 * - Role SELLER cần organization; BIDDER không cần gửi organization.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: record immutable sau khi khởi tạo.
 * - Dependency: RegisterCommand và RegisterFormValidator.
 */
public record RegisterForm(
        String customerId,
        String username,
        String email,
        String fullName,
        String password,
        String confirmPassword,
        String role,
        String organization
){};
