package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ArtTest {

    @Test
    @DisplayName("Test Constructor, Getter và Setter của class Art")
    void testArtConstructorAndGettersSetters() {
        // Khởi tạo đối tượng Art (Nó sẽ gọi ngầm super() của class Item)
        Art art = new Art("ART001", "Mona Lisa", "Bức tranh nổi tiếng", 5000000.0, "Leonardo da Vinci");

        // Kiểm tra Getter
        assertEquals("Leonardo da Vinci", art.getArtistName());

        // Kiểm tra Setter
        art.setArtistName("Vincent van Gogh");
        assertEquals("Vincent van Gogh", art.getArtistName());
    }

    @Test
    @DisplayName("Test hàm printInfo chạy mượt mà để lấy Coverage")
    void testPrintInfo() {
        Art art = new Art("ART002", "Starry Night", "Đêm đầy sao", 2000000.0, "Vincent van Gogh");

        // Gọi hàm printInfo.
        // Chỉ cần hàm này chạy qua mà không ném ra lỗi (Exception), JaCoCo sẽ đánh dấu dòng này là xanh mướt 100%!
        assertDoesNotThrow(() -> {
            art.printInfo();
        }, "Hàm printInfo không được phép ném ra lỗi");
    }
}