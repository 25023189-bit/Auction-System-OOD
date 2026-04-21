package com.auction.client.feature.auth;

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
