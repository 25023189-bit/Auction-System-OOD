package com.auction.server.service;

import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    // Khai báo Mock cho TẤT CẢ các thành phần phụ thuộc
    @Mock private UserDAO userDAO;
    @Mock private EmailService emailService;
    @Mock private PasswordStrengthValidator passwordValidator;
    @Mock private RateLimiter rateLimiter;

    // InjectMocks sẽ tự động tiêm các biến Mock ở trên vào thay thế cho các lệnh `new` trong Constructor
    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setCustomerId("U001");
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@vnu.edu.vn");
    }

    // ==========================================
    // TEST LUỒNG PROCESS FORGOT PASSWORD (Luồng chính)
    // ==========================================

    @Test
    @DisplayName("Process: Thất bại do username rỗng hoặc null")
    void processForgotPassword_EmptyUsername_ReturnsInvalid() {
        assertEquals("INVALID_USERNAME", forgotPasswordService.processForgotPassword(""));
        assertEquals("INVALID_USERNAME", forgotPasswordService.processForgotPassword(null));
    }

    @Test
    @DisplayName("Process: Bị chặn do gửi yêu cầu quá nhiều (Rate Limit)")
    void processForgotPassword_RateLimited_ReturnsRateLimited() {
        when(rateLimiter.isAllowed("testuser")).thenReturn(false);
        assertEquals("RATE_LIMITED", forgotPasswordService.processForgotPassword("testuser"));
    }

    @Test
    @DisplayName("Process: Thất bại do không tìm thấy Username trong hệ thống")
    void processForgotPassword_UserNotFound_ReturnsUserNotFound() {
        when(rateLimiter.isAllowed("testuser")).thenReturn(true);
        when(userDAO.getUserByUsernameWithEmail("testuser")).thenReturn(null);

        assertEquals("USER_NOT_FOUND", forgotPasswordService.processForgotPassword("testuser"));
    }

    @Test
    @DisplayName("Process: Thất bại do User chưa đăng ký Email")
    void processForgotPassword_NoEmail_ReturnsNoEmail() {
        sampleUser.setEmail("   "); // Email toàn khoảng trắng
        when(rateLimiter.isAllowed("testuser")).thenReturn(true);
        when(userDAO.getUserByUsernameWithEmail("testuser")).thenReturn(sampleUser);

        assertEquals("NO_EMAIL", forgotPasswordService.processForgotPassword("testuser"));
    }

    @Test
    @DisplayName("Process: Thất bại do ghi mật khẩu tạm vào Database bị lỗi")
    void processForgotPassword_DbError_ReturnsDbError() {
        when(rateLimiter.isAllowed("testuser")).thenReturn(true);
        when(userDAO.getUserByUsernameWithEmail("testuser")).thenReturn(sampleUser);
        // Bắt bất kỳ chuỗi mật khẩu ngẫu nhiên nào sinh ra cũng cho trả về FAIL
        when(userDAO.resetPasswordWithNewPassword(eq("U001"), anyString())).thenReturn("DB_FAIL");

        assertEquals("DB_ERROR", forgotPasswordService.processForgotPassword("testuser"));
    }

    @Test
    @DisplayName("Process: Thất bại do hệ thống gửi Email bị lỗi")
    void processForgotPassword_EmailFailed_ReturnsEmailFailed() {
        when(rateLimiter.isAllowed("testuser")).thenReturn(true);
        when(userDAO.getUserByUsernameWithEmail("testuser")).thenReturn(sampleUser);
        when(userDAO.resetPasswordWithNewPassword(eq("U001"), anyString())).thenReturn("SUCCESS");
        // Giả lập EmailService gặp sự cố
        when(emailService.sendPasswordResetEmail(eq("test@vnu.edu.vn"), eq("testuser"), anyString())).thenReturn(false);

        assertEquals("EMAIL_FAILED", forgotPasswordService.processForgotPassword("testuser"));
    }

    @Test
    @DisplayName("Process: Thành công hoàn hảo (Happy Path)")
    void processForgotPassword_Success() {
        when(rateLimiter.isAllowed("testuser")).thenReturn(true);
        when(userDAO.getUserByUsernameWithEmail("testuser")).thenReturn(sampleUser);
        when(userDAO.resetPasswordWithNewPassword(eq("U001"), anyString())).thenReturn("SUCCESS");
        when(emailService.sendPasswordResetEmail(eq("test@vnu.edu.vn"), eq("testuser"), anyString())).thenReturn(true);

        assertEquals("SUCCESS", forgotPasswordService.processForgotPassword("testuser"));
    }

    // ==========================================
    // TEST LUỒNG VALIDATE VÀ CẬP NHẬT MẬT KHẨU MỚI
    // ==========================================

    @Test
    @DisplayName("Validate Update: Thất bại do 2 lần nhập mật khẩu không giống nhau")
    void validateAndUpdatePassword_PasswordsDontMatch_ReturnsError() {
        assertEquals("PASSWORDS_DONT_MATCH", forgotPasswordService.validateAndUpdatePassword("U001", "newPass1", "diffPass2"));
    }

    @Test
    @DisplayName("Validate Update: Thất bại do mật khẩu không đạt độ mạnh")
    void validateAndUpdatePassword_WeakPassword_ReturnsError() {
        when(passwordValidator.isStrong("weak")).thenReturn(false);
        when(passwordValidator.getLastError()).thenReturn("Password is too short");

        String result = forgotPasswordService.validateAndUpdatePassword("U001", "weak", "weak");

        assertTrue(result.startsWith("WEAK_PASSWORD"));
        assertTrue(result.contains("Password is too short"));
    }

    @Test
    @DisplayName("Validate Update: Đổi mật khẩu thành công và gửi email xác nhận")
    void validateAndUpdatePassword_Success() {
        when(passwordValidator.isStrong("StrongP@ss1")).thenReturn(true);
        when(userDAO.resetPasswordWithNewPassword("U001", "StrongP@ss1")).thenReturn("SUCCESS");
        when(userDAO.getUserById("U001")).thenReturn(sampleUser);

        String result = forgotPasswordService.validateAndUpdatePassword("U001", "StrongP@ss1", "StrongP@ss1");

        assertEquals("SUCCESS", result);
        // Đảm bảo hàm gửi mail xác nhận đã được gọi
        verify(emailService).sendPasswordChangedConfirmation(eq("test@vnu.edu.vn"), eq("testuser"), anyString());
    }

    // ==========================================
    // TEST LUỒNG OTP (LEGACY)
    // ==========================================

    @Test
    @DisplayName("OTP Legacy: Báo lỗi khi truyền vào Token ID không tồn tại")
    void verifyOTPAndGetTemporaryPassword_InvalidToken() {
        assertEquals("INVALID_TOKEN", forgotPasswordService.verifyOTPAndGetTemporaryPassword("wrong_token_id", "123456"));
    }
}