package com.auction.client.auth;

public class RegisterFormValidator implements FormValidator<RegisterForm> {
    @Override
    public ValidationResult validate(RegisterForm form) {
        if (form.username() == null || form.username().trim().isEmpty()) {
            return ValidationResult.fail("Tên đăng nhập không được để trống!");
        }
        if (form.password() == null || form.password().isEmpty()) {
            return ValidationResult.fail("Mật khẩu không được để trống!");
        }
        if (!form.password().equals(form.confirmPassword())) {
            return ValidationResult.fail("Mật khẩu xác nhận không khớp!");
        }
        return ValidationResult.ok();
    }
}