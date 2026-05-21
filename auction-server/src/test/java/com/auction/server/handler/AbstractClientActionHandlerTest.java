package com.auction.server.handler;

import com.auction.common.dto.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractClientActionHandlerTest {

    // Tạo một class con giả lập ngay trong file test để kế thừa Abstract class
    private static class TestActionHandler extends AbstractClientActionHandler {
        protected TestActionHandler(String... supportedActions) {
            super(supportedActions);
        }

        @Override
        public void handle(Message message, ClientActionContext context) {
            // Không cần làm gì ở đây, chỉ test các hàm helper của lớp cha
        }
    }

    @Test
    @DisplayName("Test hàm canHandle kiểm tra danh sách action hỗ trợ")
    void testCanHandle() {
        TestActionHandler handler = new TestActionHandler("LOGIN", "REGISTER");

        assertTrue(handler.canHandle("LOGIN"));
        assertTrue(handler.canHandle("REGISTER"));
        assertFalse(handler.canHandle("CHAT_MSG"));
        assertFalse(handler.canHandle(null));
    }

    @Test
    @DisplayName("Test hàm parseBidAmount chuyển đổi đa kiểu dữ liệu về Double")
    void testParseBidAmount() {
        TestActionHandler handler = new TestActionHandler("TEST");

        // 1. Trường hợp client gửi kiểu Double
        assertEquals(150.5, handler.parseBidAmount(150.5));

        // 2. Trường hợp client gửi kiểu Integer
        assertEquals(200.0, handler.parseBidAmount(200));

        // 3. Trường hợp client gửi kiểu Long
        assertEquals(500.0, handler.parseBidAmount(500L));

        // 4. Trường hợp client gửi kiểu String (Cần trim khoảng trắng)
        assertEquals(123.45, handler.parseBidAmount("  123.45  "));
    }

    @Test
    @DisplayName("Test hàm generateId sinh mã ID tạm bộ nhớ")
    void testGenerateId() {
        TestActionHandler handler = new TestActionHandler("TEST");

        // Sinh ID có tiền tố là "PA" và có 6 chữ số ngẫu nhiên theo thời gian
        String id = handler.generateId("PA", 6);

        assertNotNull(id);
        assertTrue(id.startsWith("PA"));
        assertEquals(8, id.length(), "Độ dài bằng prefix (2) + digits (6) = 8");
    }
}