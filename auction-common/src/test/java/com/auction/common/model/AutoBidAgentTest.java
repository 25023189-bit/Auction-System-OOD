package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoBidAgentTest {

    @Test
    @DisplayName("Kiểm tra Constructor và Getter: Dữ liệu khởi tạo Robot phải được giữ nguyên")
    void testConstructorAndGetters() {
        // 1. Chuẩn bị dữ liệu chiến thuật mẫu
        String expectedUserId = "USER_ROBOT_01";
        double expectedMaxBid = 20000.0;
        double expectedIncrement = 150.0;

        // 2. Bơm dữ liệu vào khởi tạo Robot
        AutoBidAgent agent = new AutoBidAgent(expectedUserId, expectedMaxBid, expectedIncrement);

        // 3. Xác thực Robot đã ghi nhớ chuẩn xác 100% lệnh của chủ nhân
        assertEquals(expectedUserId, agent.getUserId(), "User ID của Robot không khớp");
        assertEquals(expectedMaxBid, agent.getMaxBid(), "Mức giá trần (Max Bid) không khớp");
        assertEquals(expectedIncrement, agent.getIncrement(), "Bước giá nhảy (Increment) không khớp");
    }
}