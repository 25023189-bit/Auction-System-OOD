package com.auction.client.feature.auth;

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
