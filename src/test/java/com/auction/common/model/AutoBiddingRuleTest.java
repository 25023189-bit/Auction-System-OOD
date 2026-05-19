package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AutoBiddingRuleTest {

    @Test
    @DisplayName("Test Constructor và chức năng tự động gán giờ mặc định")
    void testConstructorAndDefaultValues() {
        // Dùng chuỗi String ("1000") cho BigDecimal để tránh sai số thập phân
        BigDecimal maxBid = new BigDecimal("1000");
        BigDecimal step = new BigDecimal("50");

        AutoBiddingRule rule = new AutoBiddingRule("ROOM01", "USER99", maxBid, step);

        assertEquals("ROOM01", rule.getAuctionId());
        assertEquals("USER99", rule.getBidderId());
        assertEquals(maxBid, rule.getMaxBid());
        assertEquals(step, rule.getIncrementStep());

        // Biến isActive mặc định phải là true khi vừa khởi tạo
        assertTrue(rule.isActive(), "Luật AutoBidding mặc định phải được kích hoạt (true)");

        // Biến registeredAt phải được tự động gán thời gian hiện tại (không được null)
        assertNotNull(rule.getRegisteredAt(), "Thời gian đăng ký không được null do đã được set ở Constructor");
    }

    @Test
    @DisplayName("Test toàn bộ Setter và Getter còn lại")
    void testGettersAndSetters() {
        AutoBiddingRule rule = new AutoBiddingRule("A", "B", BigDecimal.ZERO, BigDecimal.ZERO);
        LocalDateTime fixedTime = LocalDateTime.now().minusDays(1);

        // Set dữ liệu mới
        rule.setRuleId(101);
        rule.setAuctionId("A_NEW");
        rule.setBidderId("B_NEW");
        rule.setMaxBid(new BigDecimal("5000"));
        rule.setIncrementStep(new BigDecimal("100"));
        rule.setActive(false); // Tắt rule
        rule.setRegisteredAt(fixedTime);

        // Kiểm chứng dữ liệu
        assertEquals(101, rule.getRuleId());
        assertEquals("A_NEW", rule.getAuctionId());
        assertEquals("B_NEW", rule.getBidderId());
        assertEquals(new BigDecimal("5000"), rule.getMaxBid());
        assertEquals(new BigDecimal("100"), rule.getIncrementStep());
        assertFalse(rule.isActive());
        assertEquals(fixedTime, rule.getRegisteredAt());
    }
}