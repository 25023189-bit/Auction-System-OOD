package com.auction.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordStrengthValidatorTest {

    private PasswordStrengthValidator validator;

    @BeforeEach
    void setUp() {
        // Luôn khởi tạo lại Validator trước mỗi bài test để làm sạch biến lastError
        validator = new PasswordStrengthValidator();
    }

    @Test
    @DisplayName("Thành công: Mật khẩu đạt chuẩn (Đủ độ dài, hoa, thường, số, ký tự đặc biệt)")
    void isStrong_ValidPassword_ReturnsTrue() {
        boolean result = validator.isStrong("StrongP@ss123");

        assertTrue(result, "Mật khẩu đúng chuẩn phải trả về true");
        // Khi thành công, lastError không bị ghi đè lỗi mới (giữ nguyên chuỗi rỗng khởi tạo)
        assertEquals("", validator.getLastError());
    }

    @Test
    @DisplayName("Thất bại: Mật khẩu là null")
    void isStrong_NullPassword_ReturnsFalse() {
        boolean result = validator.isStrong(null);

        assertFalse(result);
        assertEquals("Password must be at least 8 characters long", validator.getLastError());
    }

    @Test
    @DisplayName("Thất bại: Mật khẩu quá ngắn (< 8 ký tự)")
    void isStrong_TooShortPassword_ReturnsFalse() {
        // Mật khẩu có đủ hoa, thường, số, ký tự đặc biệt nhưng chỉ dài 6 ký tự
        boolean result = validator.isStrong("S@1abc");

        assertFalse(result);
        assertEquals("Password must be at least 8 characters long", validator.getLastError());
    }

    @Test
    @DisplayName("Thất bại: Mật khẩu thiếu chữ hoa")
    void isStrong_MissingUppercase_ReturnsFalse() {
        // Đủ độ dài, chữ thường, số, ký tự đặc biệt nhưng không có chữ hoa
        boolean result = validator.isStrong("weakp@ss123");

        assertFalse(result);
        assertEquals("Password must contain at least 1 uppercase letter", validator.getLastError());
    }

    @Test
    @DisplayName("Thất bại: Mật khẩu thiếu chữ thường")
    void isStrong_MissingLowercase_ReturnsFalse() {
        // Đủ độ dài, chữ hoa, số, ký tự đặc biệt nhưng không có chữ thường
        boolean result = validator.isStrong("WEAKP@SS123");

        assertFalse(result);
        assertEquals("Password must contain at least 1 lowercase letter", validator.getLastError());
    }

    @Test
    @DisplayName("Thất bại: Mật khẩu thiếu chữ số")
    void isStrong_MissingDigit_ReturnsFalse() {
        // Đủ độ dài, chữ hoa, chữ thường, ký tự đặc biệt nhưng không có số
        boolean result = validator.isStrong("StrongP@ssword");

        assertFalse(result);
        assertEquals("Password must contain at least 1 digit", validator.getLastError());
    }

    @Test
    @DisplayName("Thất bại: Mật khẩu thiếu ký tự đặc biệt")
    void isStrong_MissingSpecialCharacter_ReturnsFalse() {
        // Đủ độ dài, chữ hoa, chữ thường, số nhưng không có ký tự đặc biệt (!@#...)
        boolean result = validator.isStrong("StrongPass123");

        assertFalse(result);
        assertEquals("Password must contain at least 1 special character", validator.getLastError());
    }
}