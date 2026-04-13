package com.auction.client.feature.auth;

// Không cần import ValidationResult nếu nó đã nằm cùng folder auth
// Nếu vẫn báo đỏ, hãy dùng: import com.auction.client.feature.auth.ValidationResult;

public class RegisterFormValidator implements FormValidator<RegisterForm> {

    @Override
    public ValidationResult validate(RegisterForm form) {
        // 1. Kiểm tra customerId (Vì Controller truyền vào customerId làm username)
        if (form.customerId() == null || form.customerId().trim().isEmpty()) {
            return ValidationResult.fail("Tên đăng nhập không được để trống!");
        }

        // 2. Kiểm tra Password
        if (form.password() == null || form.password().isEmpty()) {
            return ValidationResult.fail("Mật khẩu không được để trống!");
        }

        // 3. Kiểm tra Xác nhận mật khẩu
        if (!form.password().equals(form.confirmPassword())) {
            return ValidationResult.fail("Mật khẩu xác nhận không khớp!");
        }

        return ValidationResult.ok();
    }
}