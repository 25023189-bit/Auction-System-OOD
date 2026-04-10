package com.auction.client.auth;

public class ResetPasswordFormValidator implements FormValidator<ResetPasswordForm> {
    @Override
    public ValidationResult validate(ResetPasswordForm form) {
        if (form.username() == null || form.username().trim().isEmpty()) {
            return ValidationResult.fail("Vui lòng nhập tên tài khoản!");
        }
        if (form.newPassword() == null || form.newPassword().trim().isEmpty()) {
            return ValidationResult.fail("Vui lòng nhập mật khẩu mới!");
        }
        if (!form.newPassword().equals(form.confirmPassword())) {
            return ValidationResult.fail("Mật khẩu xác nhận không khớp!");
        }
        return ValidationResult.ok();
    }
}