package com.auction.client.auth;

public class LoginFormValidator implements FormValidator<LoginForm> {
    @Override
    public ValidationResult validate(LoginForm form) {
        if (form.username() == null || form.username().trim().isEmpty()
                || form.password() == null || form.password().trim().isEmpty()) {
            return ValidationResult.fail("Vui lòng nhập đủ thông tin!");
        }
        return ValidationResult.ok();
    }
}