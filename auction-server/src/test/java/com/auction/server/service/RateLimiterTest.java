package com.auction.server.service;

import com.auction.server.config.ConfigManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimiterTest {

    private MockedStatic<ConfigManager> mockedConfigManager;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Giả lập cấu hình: Tối đa 2 lần request trong vòng 5 phút
        ConfigManager mockConfig = mock(ConfigManager.class);
        when(mockConfig.getRateLimitRequests()).thenReturn(2);
        when(mockConfig.getRateLimitWindowMinutes()).thenReturn(5);

        // Bắt cóc hàm getInstance() để tiêm cấu hình giả vào
        mockedConfigManager = Mockito.mockStatic(ConfigManager.class);
        mockedConfigManager.when(ConfigManager::getInstance).thenReturn(mockConfig);

        rateLimiter = new RateLimiter();
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp MockedStatic để không ảnh hưởng các test class khác
        mockedConfigManager.close();
    }

    @Test
    @DisplayName("isAllowed: Cho phép request khi vẫn còn trong giới hạn (Quota)")
    void isAllowed_UnderLimit_ReturnsTrue() {
        // Dùng một ID riêng biệt cho bài test này để không bị dính dữ liệu tĩnh
        String userId = "USER_ALLOW_TEST";

        assertTrue(rateLimiter.isAllowed(userId), "Lần 1: Phải được cho phép");
        assertTrue(rateLimiter.isAllowed(userId), "Lần 2: Vẫn còn quota, phải được cho phép");
    }

    @Test
    @DisplayName("isAllowed: Chặn request khi VƯỢT QUÁ giới hạn số lần")
    void isAllowed_OverLimit_ReturnsFalse() {
        String userId = "USER_BLOCK_TEST";

        // Dùng hết 2 lượt cho phép
        rateLimiter.isAllowed(userId);
        rateLimiter.isAllowed(userId);

        // Lần thứ 3 sẽ bị chặn đứng
        assertFalse(rateLimiter.isAllowed(userId), "Lần 3: Phải bị chặn vì vượt quá 2 lần/5 phút");
    }

    @Test
    @DisplayName("getRemainingRequests: Tính toán số lượt còn lại chính xác")
    void getRemainingRequests_CalculatesCorrectly() {
        String userId = "USER_REMAINING_TEST";

        assertEquals(2, rateLimiter.getRemainingRequests(userId), "Ban đầu phải còn đúng 2 lượt");

        rateLimiter.isAllowed(userId); // Dùng 1 lượt
        assertEquals(1, rateLimiter.getRemainingRequests(userId), "Sau khi dùng 1 lần, phải còn 1 lượt");

        rateLimiter.isAllowed(userId); // Dùng nốt lượt cuối
        assertEquals(0, rateLimiter.getRemainingRequests(userId), "Đã dùng hết, phải báo còn 0 lượt");

        rateLimiter.isAllowed(userId); // Cố tình gọi vượt mức
        assertEquals(0, rateLimiter.getRemainingRequests(userId), "Dù cố spam thì số lượt còn lại không được phép âm");
    }

    @Test
    @DisplayName("Dọn dẹp Request cũ: Reset số lượt khi du hành thời gian qua cửa sổ Rate Limit")
    void rateLimiter_TimeTravel_CleansOldRequests() {
        String userId = "USER_TIME_TRAVEL_TEST";

        // Tạo một mốc thời gian tĩnh để làm điểm bắt đầu
        LocalDateTime fixedStartTime = LocalDateTime.of(2026, 5, 19, 10, 0, 0);

        // Bắt cóc hàm LocalDateTime.now() để chúng ta tự do thao túng thời gian
        try (MockedStatic<LocalDateTime> mockedTime = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {

            // 1. Ở thời điểm hiện tại: Gọi hết sạch 2 lượt
            mockedTime.when(LocalDateTime::now).thenReturn(fixedStartTime);
            rateLimiter.isAllowed(userId);
            rateLimiter.isAllowed(userId);

            assertEquals(0, rateLimiter.getRemainingRequests(userId), "Hiện tại: Phải cạn kiệt lượt");

            // 2. DU HÀNH THỜI GIAN: Đi tới tương lai 6 phút sau (Vượt qua window 5 phút)
            LocalDateTime futureTime = fixedStartTime.plusMinutes(6);
            mockedTime.when(LocalDateTime::now).thenReturn(futureTime);

            // 3. Kiểm tra lại: Hệ thống phải tự dọn dẹp và reset lại thành 2 lượt ban đầu
            assertEquals(2, rateLimiter.getRemainingRequests(userId), "Tương lai: Các request cũ phải bị xóa, reset lại 2 lượt");
            assertTrue(rateLimiter.isAllowed(userId), "Phải tiếp tục được cho phép request");
        }
    }
}