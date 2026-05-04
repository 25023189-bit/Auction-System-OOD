package com.auction.client.feature.auth;

/**
 * Validator kiểm tra dữ liệu form đăng ký.
 *
 * Vai trò:
 * - Kiểm tra username/password và xác nhận mật khẩu.
 * - Áp dụng điều kiện organization bắt buộc cho seller.
 *
 * Luồng chính:
 * 1. RegisterCommand gọi validate(RegisterForm) khi user submit đăng ký.
 * 2. Validator trả fail ở lỗi đầu tiên hoặc ok nếu form đủ điều kiện gửi server.
 *
 * Business rules:
 * - Username và password là bắt buộc.
 * - Seller bắt buộc có organization vì thông tin này dùng cho xét duyệt phiên đấu giá.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ dùng biến local.
 * - Dependency: FormValidator<RegisterForm>, ValidationResult.
 */
public class RegisterFormValidator implements FormValidator<RegisterForm> {

    @Override
    public ValidationResult validate(RegisterForm form) {
        if (form.username() == null || form.username().trim().isEmpty()) {
            return ValidationResult.fail("Username is required!");
        }

        if (form.password() == null || form.password().isEmpty()) {
            return ValidationResult.fail("Password is required!");
        }

        if (!form.password().equals(form.confirmPassword())) {
            return ValidationResult.fail("Password confirmation does not match!");
        }

        if ("SELLER".equalsIgnoreCase(form.role())) {
            if (form.organization() == null || form.organization().trim().isEmpty()) {
                return ValidationResult.fail("Seller organization is required!");
            }
        }

        return ValidationResult.ok();
    }
}
