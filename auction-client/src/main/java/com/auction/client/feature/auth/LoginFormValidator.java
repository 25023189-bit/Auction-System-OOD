package com.auction.client.feature.auth;

/**
 * Validator kiểm tra dữ liệu form đăng nhập.
 *
 * Vai trò:
 * - Bảo đảm login identifier không rỗng.
 * - Bảo đảm password không rỗng trước khi gửi request.
 *
 * Luồng chính:
 * 1. AuthActionFacade.login gọi validate(LoginForm) khi user submit.
 * 2. Validator trả fail ở lỗi đầu tiên hoặc ok nếu đủ dữ liệu.
 *
 * Business rules:
 * - Username là bắt buộc.
 * - Password là bắt buộc và không được chỉ chứa khoảng trắng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ dùng biến local.
 * - Dependency: FormValidator<LoginForm>, ValidationResult.
 */
public class LoginFormValidator implements FormValidator<LoginForm> {

    @Override
    public ValidationResult validate(LoginForm form) {
        if (form.username() == null || form.username().trim().isEmpty()) {
            return ValidationResult.fail("Username is required!");
        }

        if (form.password() == null || form.password().trim().isEmpty()) {
            return ValidationResult.fail("Password is required!");
        }

        return ValidationResult.ok();
    }
}
