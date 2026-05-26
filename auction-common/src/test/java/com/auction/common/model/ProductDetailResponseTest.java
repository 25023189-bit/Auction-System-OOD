package com.auction.common.model;

import com.auction.common.dto.BidHistoryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductDetailResponseTest {

    @Test
    @DisplayName("Test Constructor mặc định và toàn bộ Getter/Setter")
    void testGettersAndSetters() {
        ProductDetailResponse response = new ProductDetailResponse();

        // 1. Kiểm tra giá trị mặc định lúc mới khởi tạo
        assertNull(response.getTitle());
        assertNull(response.getDescription());
        assertNull(response.getAuthor());
        assertEquals(0.0, response.getStartPrice());
        assertEquals(0.0, response.getCurrentPrice());
        assertEquals(0L, response.getTimeLeftMillis());
        assertNull(response.getBase64Image());
        assertNull(response.getBidHistory());

        // Tạo một danh sách lịch sử đấu giá giả để test
        List<BidHistoryDTO> mockHistory = new ArrayList<>();
        mockHistory.add(new BidHistoryDTO("HanToVIP", 1500.0, "19/05/2026 02:25:00"));

        // 2. Bơm toàn bộ dữ liệu vào qua Setter
        response.setTitle("Tranh Sơn Dầu Đêm Đầy Sao");
        response.setDescription("Bức tranh gốc siêu hiếm");
        response.setAuthor("Vincent van Gogh");
        response.setStartPrice(1000.0);
        response.setCurrentPrice(1500.0);
        response.setTimeLeftMillis(3600000L); // Còn 1 tiếng
        response.setBase64Image("encoded-image");
        response.setBidHistory(mockHistory);

        // 3. Rút dữ liệu ra qua Getter để lấy trọn điểm Coverage
        assertEquals("Tranh Sơn Dầu Đêm Đầy Sao", response.getTitle());
        assertEquals("Bức tranh gốc siêu hiếm", response.getDescription());
        assertEquals("Vincent van Gogh", response.getAuthor());
        assertEquals(1000.0, response.getStartPrice());
        assertEquals(1500.0, response.getCurrentPrice());
        assertEquals(3600000L, response.getTimeLeftMillis());
        assertEquals("encoded-image", response.getBase64Image());

        // Kiểm tra list BidHistoryDTO
        assertEquals(1, response.getBidHistory().size());
        assertEquals("HanToVIP", response.getBidHistory().get(0).getBidderName());
        assertEquals(1500.0, response.getBidHistory().get(0).getAmount());
    }
}
