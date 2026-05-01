package com.auction.client.feature.auth;

/**
 * Giá trị kết quả validate form phía client.
 *
 * Vai trò:
 * - Đóng gói cờ hợp lệ và thông báo lỗi nếu có.
 * - Cung cấp factory ok()/fail() để validator trả kết quả thống nhất.
 *
 * Luồng chính:
 * 1. FormValidator tạo ValidationResult sau khi kiểm tra input.
 * 2. Command đọc isValid()/getMessage() để gửi request hoặc hiển thị lỗi.
 *
 * Business rules:
 * - valid=false phải đi kèm message rõ ràng cho người dùng.
 * - ok() trả message rỗng vì không có lỗi cần hiển thị.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
 * - Dependency: FormValidator và các command xác thực.
 */
public class ValidationResult {
    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
