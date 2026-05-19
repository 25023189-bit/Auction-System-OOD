package com.auction.server.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLoggerTest {

    // Sao lưu lại luồng in mặc định của hệ thống
    private final PrintStream standardOut = System.out;

    // Tạo một luồng đầu ra giả để hứng log
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        // Chuyển hướng System.out vào biến outputStreamCaptor của chúng ta
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        // Test xong phải trả lại luồng in mặc định cho hệ thống, nếu không các test khác sẽ không in ra màn hình được
        System.setOut(standardOut);
    }

    // ==========================================
    // TEST CHE GIẤU EMAIL (MASK EMAIL)
    // ==========================================

    @Test
    @DisplayName("Log Requested: Định dạng chuẩn và mask email hợp lệ chính xác")
    void logPasswordResetRequested_ValidEmail_MasksCorrectly() {
        // Ví dụ: han.to@vnu.edu.vn -> h****o@vn***
        AuditLogger.logPasswordResetRequested("han123", "han.to@vnu.edu.vn");

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("[AUDIT]"));
        assertTrue(output.contains("han123"));
        assertTrue(output.contains("h****o@vn***"), "Email không được che giấu đúng chuẩn");
    }

    @Test
    @DisplayName("Log Requested: Xử lý an toàn email null, email ngắn hoặc sai định dạng")
    void logPasswordResetRequested_InvalidEmail_UsesFallbackMask() {
        // Trường hợp 1: Email là null
        AuditLogger.logPasswordResetRequested("han123", null);
        assertTrue(outputStreamCaptor.toString().contains("***@***"));
        outputStreamCaptor.reset(); // Xóa log cũ để test case tiếp theo

        // Trường hợp 2: Email quá ngắn (< 5 ký tự)
        AuditLogger.logPasswordResetRequested("han123", "a@b");
        assertTrue(outputStreamCaptor.toString().contains("***@***"));
        outputStreamCaptor.reset();

        // Trường hợp 3: Email không có chữ @
        AuditLogger.logPasswordResetRequested("han123", "notanemail");
        assertTrue(outputStreamCaptor.toString().contains("***@***"));
    }

    // ==========================================
    // TEST CHE GIẤU TOKEN VÀ CÁC LOG KHÁC
    // ==========================================

    @Test
    @DisplayName("Log Token Generated: Cắt đúng 8 ký tự đầu của token để bảo mật")
    void logPasswordResetTokenGenerated_ValidToken_TruncatesProperly() {
        String longToken = "1234567890ABCDEF-SECRET-KEY";
        AuditLogger.logPasswordResetTokenGenerated("han123", longToken);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("token: 12345678...)"), "Log không cắt đúng 8 ký tự đầu của token");
    }

    @Test
    @DisplayName("Log Invalid Token: Ghi nhận cảnh báo và cắt đúng 8 ký tự token")
    void logInvalidTokenAttempt_ValidToken_TruncatesProperly() {
        AuditLogger.logInvalidTokenAttempt("han123", "ABCDEFGH-INVALID-KEY");

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Invalid/expired token used by user: han123"));
        assertTrue(output.contains("token: ABCDEFGH...)"));
    }

    @Test
    @DisplayName("Log Success: Ghi log thành công chuẩn xác")
    void logPasswordResetSuccess_PrintsCorrectly() {
        AuditLogger.logPasswordResetSuccess("han123");

        assertTrue(outputStreamCaptor.toString().contains("Password reset SUCCESS for user: han123"));
    }

    @Test
    @DisplayName("Log Failure: Ghi log thất bại kèm nguyên nhân rõ ràng")
    void logPasswordResetFailure_PrintsReasonCorrectly() {
        AuditLogger.logPasswordResetFailure("han123", "Token expired or not found");

        assertTrue(outputStreamCaptor.toString().contains("FAILED for user: han123 - Reason: Token expired or not found"));
    }

    @Test
    @DisplayName("Log Changed: Ghi log đổi mật khẩu kèm phương thức thay đổi")
    void logPasswordChanged_PrintsChangeTypeCorrectly() {
        AuditLogger.logPasswordChanged("han123", "FORGOT_PASSWORD_FLOW");

        assertTrue(outputStreamCaptor.toString().contains("Password changed for user: han123 (FORGOT_PASSWORD_FLOW)"));
    }
}