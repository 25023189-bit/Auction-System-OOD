package com.auction.client.feature.auth;

/**
 * Validator cho form đăng nhập.
 */
public class LoginFormValidator implements FormValidator<LoginForm> {

    @Override
    public ValidationResult validate(LoginForm form) {
        if (form.customerId() == null || form.customerId().trim().isEmpty()) {
            return ValidationResult.fail("Username is required!");
        }

        if (form.password() == null || form.password().trim().isEmpty()) {
            return ValidationResult.fail("Password is required!");
        }

        return ValidationResult.ok();
    }
}
