package com.auction.client.feature.auth;

public class LoginFormValidator implements FormValidator<LoginForm> {

    @Override
    public ValidationResult validate(LoginForm form) {
        // Sử dụng customerId() thay vì getUsername()
        if (form.customerId() == null || form.customerId().trim().isEmpty()) {
            return ValidationResult.fail("Tên đăng nhập không được để trống!");
        }

        // Sử dụng password() thay vì getPassword()
        if (form.password() == null || form.password().trim().isEmpty()) {
            return ValidationResult.fail("Mật khẩu không được để trống!");
        }

        return ValidationResult.ok();
    }
}