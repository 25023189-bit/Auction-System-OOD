package com.auction.client.network.dispatcher;

import com.auction.client.network.messaging.MessageHandler;
import com.auction.common.dto.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MessageRouteResultTest {

    @Test
    @DisplayName("Tạo kết quả MATCHED: Phải lưu trữ đầy đủ Handler và Message")
    void testMatchedFactoryMethod() {
        // 1. Chuẩn bị "đồ giả"
        MessageHandler mockHandler = mock(MessageHandler.class);
        Message mockMessage = new Message("CHAT_MSG", "user1", "Hello");

        // 2. Dùng hàm Static Factory để tạo đối tượng
        MessageRouteResult result = MessageRouteResult.matched(mockHandler, mockMessage);

        // 3. Xác thực dữ liệu được giữ nguyên vẹn
        assertTrue(result.hasHandler(), "hasHandler() phải trả về true vì đã truyền Handler vào");
        assertEquals(mockHandler, result.getHandler(), "Handler trả về không khớp với bản gốc");
        assertEquals(mockMessage, result.getMessage(), "Message trả về không khớp với bản gốc");
    }

    @Test
    @DisplayName("Tạo kết quả UNMATCHED: Không có Handler, chỉ chứa Message gốc")
    void testUnmatchedFactoryMethod() {
        // 1. Chỉ chuẩn bị Message, không có Handler
        Message mockMessage = new Message("UNKNOWN_ACTION", "user1", "Data");

        // 2. Tạo đối tượng nhánh Unmatched
        MessageRouteResult result = MessageRouteResult.unmatched(mockMessage);

        // 3. Xác thực trạng thái an toàn
        assertFalse(result.hasHandler(), "hasHandler() phải trả về false vì không có Handler");
        assertNull(result.getHandler(), "getHandler() phải trả về null");
        assertEquals(mockMessage, result.getMessage(), "Message gốc vẫn phải được giữ lại để ghi log");
    }
}