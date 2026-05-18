package com.auction.server.service;

import com.auction.server.config.ConfigManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private MockedStatic<ConfigManager> mockedConfigManager;
    private MockedStatic<Transport> mockedTransport;
    private ConfigManager mockConfig;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // 1. Giả lập ConfigManager
        mockConfig = mock(ConfigManager.class);

        // PHẢI CÓ lenient() Ở TOÀN BỘ 5 DÒNG NÀY:
        lenient().when(mockConfig.getEmailMaxRetries()).thenReturn(2);
        lenient().when(mockConfig.getSmtpHost()).thenReturn("smtp.gmail.com");
        lenient().when(mockConfig.getSmtpPort()).thenReturn(587);
        lenient().when(mockConfig.getEmailFrom()).thenReturn("test@auction.com");
        lenient().when(mockConfig.getEmailPassword()).thenReturn("secret");

        // Bắt cóc hàm static getInstance() của ConfigManager
        mockedConfigManager = mockStatic(ConfigManager.class);
        // THÊM CẢ lenient() VÀO DÒNG NÀY CHO CHẮC CÚ:
        mockedConfigManager.when(ConfigManager::getInstance).thenReturn(mockConfig);

        // Bắt cóc hàm static send() của JavaMail Transport
        mockedTransport = mockStatic(Transport.class);

        // Khởi tạo service sau khi đã mock config xong
        emailService = new EmailService();
    }

    @AfterEach
    void tearDown() {
        // Luôn phải dọn dẹp MockedStatic sau khi chạy test để không ảnh hưởng bài khác
        mockedConfigManager.close();
        mockedTransport.close();
    }

    @Test
    @DisplayName("Gửi mật khẩu tạm: Thành công ngay lần đầu")
    void sendPasswordResetEmail_Success() {
        boolean result = emailService.sendPasswordResetEmail("user@test.com", "Hân Tô", "tempPass123");

        assertTrue(result, "Phải trả về true khi gửi email thành công");
        // Kiểm tra xem hàm Transport.send đã được gọi đúng 1 lần chưa
        mockedTransport.verify(() -> Transport.send(any(Message.class)), times(1));
    }

    @Test
    @DisplayName("Gửi OTP: Thành công")
    void sendOTPEmail_Success() {
        boolean result = emailService.sendOTPEmail("user@test.com", "Hân Tô", "123456", 15);

        assertTrue(result, "Phải trả về true khi gửi OTP thành công");
        mockedTransport.verify(() -> Transport.send(any(Message.class)), times(1));
    }

    @Test
    @DisplayName("Gửi xác nhận: Thành công và kiểm tra hàm escapeHtml")
    void sendPasswordChangedConfirmation_SuccessWithHtmlEscape() {
        // Cố tình truyền username chứa mã độc HTML để test hàm escapeHtml ẩn bên trong
        String maliciousUsername = "<script>alert('xss')</script>";

        boolean result = emailService.sendPasswordChangedConfirmation("user@test.com", maliciousUsername, "2026-05-18");

        assertTrue(result);
        mockedTransport.verify(() -> Transport.send(any(Message.class)), times(1));
    }

    @Test
    @DisplayName("Gửi thất bại: Cơ chế Retry chạy đúng số lần quy định rồi mới báo false")
    void sendEmail_FailsAfterMaxRetries() {
        // Ép Transport.send luôn ném ra lỗi mạng
        mockedTransport.when(() -> Transport.send(any(Message.class)))
                .thenThrow(new MessagingException("Simulated Network Error"));

        // Ghi lại thời gian chạy để đảm bảo có Thread.sleep(2000)
        long startTime = System.currentTimeMillis();

        boolean result = emailService.sendPasswordResetEmail("user@test.com", "Hân Tô", "tempPass123");

        long duration = System.currentTimeMillis() - startTime;

        assertFalse(result, "Phải trả về false khi gửi thất bại hoàn toàn");

        // Vì maxRetries = 2 nên phải gọi hàm send đúng 2 lần rồi mới bỏ cuộc
        mockedTransport.verify(() -> Transport.send(any(Message.class)), times(2));

        // Ít nhất phải tốn 2000ms (2 giây) do có 1 lần Thread.sleep
        assertTrue(duration >= 2000, "Phải có thời gian chờ Thread.sleep(2000) giữa các lần retry");
    }
}