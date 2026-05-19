package com.auction.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BidHistoryDTOTest {

    @Test
    @DisplayName("Test Constructor có tham số")
    void testParameterizedConstructor() {
        BidHistoryDTO dto = new BidHistoryDTO("HanTo", 5000.0, "2026-05-19 02:00:00");

        assertEquals("HanTo", dto.getBidderName());
        assertEquals(5000.0, dto.getAmount());
        assertEquals("2026-05-19 02:00:00", dto.getTime());
    }

    @Test
    @DisplayName("Test Constructor mặc định và toàn bộ Getter/Setter")
    void testDefaultConstructorAndSetters() {
        BidHistoryDTO dto = new BidHistoryDTO();

        // Kiểm tra giá trị mặc định lúc mới khởi tạo bằng Constructor rỗng
        assertNull(dto.getBidderName());
        assertEquals(0.0, dto.getAmount());
        assertNull(dto.getTime());

        // Bơm dữ liệu vào qua Setter
        dto.setBidderName("UserVIP");
        dto.setAmount(12500.0);
        dto.setTime("2026-05-19 02:15:30");

        // Rút dữ liệu ra qua Getter để kiểm chứng
        assertEquals("UserVIP", dto.getBidderName());
        assertEquals(12500.0, dto.getAmount());
        assertEquals("2026-05-19 02:15:30", dto.getTime());
    }
}