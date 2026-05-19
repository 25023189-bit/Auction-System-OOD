package com.auction.server.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("Test mã hóa mật khẩu và đối chiếu kiểm tra đúng sai với BCrypt")
    void testHashAndCheckPassword() {
        String rawPassword = "BaoHanUET_2026!";

        // 1. Tiến hành hash mật khẩu thô
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword, "Mật khẩu sau khi hash phải khác mật khẩu thô");
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$"), "Định dạng BCrypt phải chuẩn");

        // 2. Kiểm tra đối chiếu mật khẩu chuẩn (Phải trả về true)
        assertTrue(PasswordUtil.checkPassword(rawPassword, hashedPassword), "Mật khẩu đúng phải khớp");

        // 3. Kiểm tra đối chiếu sai mật khẩu (Phải trả về false)
        assertFalse(PasswordUtil.checkPassword("SaiMatKhau rồi", hashedPassword), "Mật khẩu sai không được phép khớp");
    }
}