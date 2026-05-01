package com.auction.client.feature.auth;

/**
 * Validator kiểm tra form quên mật khẩu/đặt lại mật khẩu.
 *
 * Vai trò:
 * - Kiểm tra username và mật khẩu mới không rỗng.
 * - Kiểm tra mật khẩu xác nhận khớp với mật khẩu mới.
 *
 * Luồng chính:
 * 1. ResetPasswordCommand gọi validate(ResetPasswordForm).
 * 2. Validator trả fail ở lỗi đầu tiên hoặc ok nếu có thể gửi request.
 *
 * Business rules:
 * - Username là bắt buộc để server xác định tài khoản.
 * - Password confirmation phải khớp trước khi gửi lên server.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ dùng biến local.
 * - Dependency: FormValidator<ResetPasswordForm>, ValidationResult.
 */
public class ResetPasswordFormValidator implements FormValidator<ResetPasswordForm> {
    @Override
    public ValidationResult validate(ResetPasswordForm form) {
        if (form.username() == null || form.username().trim().isEmpty()) {
            return ValidationResult.fail("Please enter your username!");
        }
        if (form.newPassword() == null || form.newPassword().trim().isEmpty()) {
            return ValidationResult.fail("Please enter a new password!");
        }
        if (!form.newPassword().equals(form.confirmPassword())) {
            return ValidationResult.fail("Password confirmation does not match!");
        }
        return ValidationResult.ok();
    }
}
