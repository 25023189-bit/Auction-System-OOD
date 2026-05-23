package com.auction.client.feature.controllers.viewmodel;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LobbyRoomDisplayModelTest {

    @Test
    @DisplayName("Test Constructor khởi tạo và chức năng tự động định dạng hiển thị giá tiền")
    void testConstructorAndFormatting() {
        // Khởi tạo model phòng đấu giá với số tiền cụ thể
        LobbyRoomDisplayModel model = new LobbyRoomDisplayModel("AU100001", "Bức Tranh Mona Lisa", 1500000.0);

        // Kiểm tra các trường dữ liệu thông thường thông qua hàm Getter
        assertEquals("AU100001", model.getRoomId());
        assertEquals("Bức Tranh Mona Lisa", model.getItemName());
        assertEquals(1500000.0, model.getCurrentPrice());

        // Kiểm tra chuỗi định dạng hiển thị tiền tệ (displayPrice)
        String displayPrice = model.getDisplayPrice();
        assertNotNull(displayPrice);

        // Sử dụng Regex chấp nhận cả định dạng 1,500,000 $ (US/UK) hoặc 1.500.000 $ (VN) để chống lỗi Locale lúc biên dịch
        assertTrue(displayPrice.matches("1[.,]500[.,]000 \\$"),
                "Chuỗi hiển thị giá tiền không khớp định dạng mong muốn. Thực tế là: " + displayPrice);
    }
}
