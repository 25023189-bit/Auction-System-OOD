package com.auction.client.feature.auth;

public class RegisterFormValidator implements FormValidator<RegisterForm> {

    @Override
    public ValidationResult validate(RegisterForm form) {
        if (form.username() == null || form.username().trim().isEmpty()) {
            return ValidationResult.fail("Ten dang nhap khong duoc de trong!");
        }

        if (form.password() == null || form.password().isEmpty()) {
            return ValidationResult.fail("Mat khau khong duoc de trong!");
        }

        if (!form.password().equals(form.confirmPassword())) {
            return ValidationResult.fail("Mat khau xac nhan khong khop!");
        }

        if ("SELLER".equalsIgnoreCase(form.role())) {
            if (form.organization() == null || form.organization().trim().isEmpty()) {
                return ValidationResult.fail("Seller bat buoc phai co to chuc!");
            }
        }

        return ValidationResult.ok();
    }
}
