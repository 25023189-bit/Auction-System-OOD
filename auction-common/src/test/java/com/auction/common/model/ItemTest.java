package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemTest {

    @Test
    @DisplayName("Test Constructor mặc định (Không tham số)")
    void testDefaultConstructor() {
        Item item = new Item();

        // Vì không có Setter, các biến sẽ mang giá trị mặc định của Java (null với Object, 0.0 với số thực)
        assertNull(item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertEquals(0.0, item.getStartingPrice());
    }

    @Test
    @DisplayName("Test Constructor có tham số và toàn bộ các hàm Alias (Bí danh)")
    void testParameterizedConstructorAndAliases() {
        // Bơm dữ liệu 1 lần duy nhất qua Constructor
        Item item = new Item("ITEM_99", "Bàn phím cơ", "Gõ cực nảy", 1200.5);

        // 1. Kiểm tra các hàm Getters gốc
        assertEquals("ITEM_99", item.getId());
        assertEquals("Bàn phím cơ", item.getName());
        assertEquals("Gõ cực nảy", item.getDescription());
        assertEquals(1200.5, item.getStartingPrice());

        // 2. Kiểm tra các hàm Alias (Bí danh) được thiết kế riêng
        assertEquals("ITEM_99", item.getProductId(), "getProductId() phải trỏ về id");
        assertEquals("Bàn phím cơ", item.getProductName(), "getProductName() phải trỏ về name");

        // Hàm mock tạm thời để fix lỗi bên Art.java
        assertEquals(1200.5, item.getCurrentHighestPrice(), "getCurrentHighestPrice() đang được code cứng trả về startingPrice");
    }
}