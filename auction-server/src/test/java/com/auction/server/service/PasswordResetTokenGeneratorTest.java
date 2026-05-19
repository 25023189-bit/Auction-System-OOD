package com.auction.server.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenGeneratorTest {

    // ==========================================
    // TEST LUỒNG SINH DỮ LIỆU NGẪU NHIÊN
    // ==========================================

    @Test
    @DisplayName("Sinh Token: Không bị null, đủ độ dài (43 chars cho 32 bytes Base64) và URL-safe")
    void generateToken_ProducesValidBase64UrlString() {
        String token = PasswordResetTokenGenerator.generateToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // 32 bytes encode Base64 không có padding (=) sẽ ra 43 ký tự
        assertEquals(43, token.length());
        assertFalse(token.contains("="), "Không được chứa ký tự padding = của Base64 thường");
    }

    @Test
    @DisplayName("Sinh OTP: Luôn ra chuẩn 6 chữ số (Kể cả khi sinh số nhỏ, hàm vẫn tự động đệm số 0)")
    void generateOTP_ProducesExactly6Digits() {
        // Sinh thử 100 lần để đảm bảo tính ngẫu nhiên không làm hỏng format
        for (int i = 0; i < 100; i++) {
            String otp = PasswordResetTokenGenerator.generateOTP();
            assertEquals(6, otp.length(), "OTP luôn phải dài đúng 6 ký tự");
            assertTrue(otp.matches("\\d{6}"), "OTP chỉ được chứa các chữ số từ 0-9");
        }
    }

    @Test
    @DisplayName("Sinh Mật khẩu tạm: Đạt chuẩn 12 ký tự")
    void generateTemporaryPassword_ProducesExactly12Chars() {
        String tempPass = PasswordResetTokenGenerator.generateTemporaryPassword();

        assertNotNull(tempPass);
        assertEquals(12, tempPass.length(), "Mật khẩu tạm luôn phải dài 12 ký tự");
    }

    // ==========================================
    // TEST LUỒNG TẠO VÀ QUẢN LÝ MODEL TOKEN
    // ==========================================

    @Test
    @DisplayName("Tạo Token Model: Khởi tạo đúng dữ liệu và thời hạn")
    void createToken_MapsDataAndExpirationCorrectly() {
        PasswordResetTokenGenerator.PasswordResetToken tokenObj =
                PasswordResetTokenGenerator.createToken("U001", "test@vnu.edu.vn", 15);

        assertNotNull(tokenObj.getToken());
        assertNotNull(tokenObj.getOtp());
        assertEquals("U001", tokenObj.getUserId());
        assertEquals("test@vnu.edu.vn", tokenObj.getEmail());

        // Token mới tạo phải hợp lệ và chưa hết hạn
        assertFalse(tokenObj.isExpired());
        assertTrue(tokenObj.isValid());
        assertFalse(tokenObj.isUsed());
    }

    @Test
    @DisplayName("Token Model: Xử lý hết hạn chính xác khi truyền mốc thời gian quá khứ")
    void tokenModel_Expired_ReturnsFalseForValid() {
        // Cố tình tạo token với thời điểm hết hạn (expiresAt) là 5 phút trước
        PasswordResetTokenGenerator.PasswordResetToken expiredToken =
                new PasswordResetTokenGenerator.PasswordResetToken(
                        "fakeToken", "123456", "U001", "test@vnu.edu.vn",
                        LocalDateTime.now().minusMinutes(5)
                );

        assertTrue(expiredToken.isExpired(), "Token phải bị báo hết hạn");
        assertFalse(expiredToken.isValid(), "Token hết hạn thì không còn hợp lệ");
    }

    @Test
    @DisplayName("Token Model: Hàm markAsUsed phải vô hiệu hóa được Token dù chưa hết hạn")
    void tokenModel_MarkAsUsed_InvalidatesToken() {
        // Tạo token mới có thời hạn tới tận 10 phút nữa
        PasswordResetTokenGenerator.PasswordResetToken activeToken =
                new PasswordResetTokenGenerator.PasswordResetToken(
                        "fakeToken", "123456", "U001", "test@vnu.edu.vn",
                        LocalDateTime.now().plusMinutes(10)
                );

        // Ban đầu phải hợp lệ
        assertTrue(activeToken.isValid());
        assertFalse(activeToken.isUsed());

        // Đánh dấu đã sử dụng
        activeToken.markAsUsed();

        // Kiểm tra sau khi đánh dấu
        assertTrue(activeToken.isUsed(), "Cờ used phải được bật thành true");
        assertFalse(activeToken.isValid(), "Token đã dùng rồi thì isValid phải báo false");
    }
}