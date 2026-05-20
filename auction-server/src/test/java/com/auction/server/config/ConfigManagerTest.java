package com.auction.server.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    @Test
    @DisplayName("Test khởi tạo Singleton và đọc giá trị cấu hình mặc định an toàn")
    void testGetInstanceAndDefaultValues() {
        ConfigManager config1 = ConfigManager.getInstance();
        ConfigManager config2 = ConfigManager.getInstance();

        // 1. Kiểm tra Singleton (2 lần gọi phải ra cùng 1 instance)
        assertNotNull(config1);
        assertSame(config1, config2);

        // 2. Kiểm tra các hàm getter không bao giờ bị crash và có giá trị fallback an toàn
        assertNotNull(config1.getEmailFrom());
        assertNotNull(config1.getEmailPassword());
        assertNotNull(config1.getSmtpHost());
        assertTrue(config1.getSmtpPort() > 0);
        assertTrue(config1.getEmailMaxRetries() > 0);
        assertTrue(config1.getTokenExpirationMinutes() > 0);
        assertTrue(config1.getRateLimitRequests() > 0);
        assertTrue(config1.getRateLimitWindowMinutes() > 0);
    }
}