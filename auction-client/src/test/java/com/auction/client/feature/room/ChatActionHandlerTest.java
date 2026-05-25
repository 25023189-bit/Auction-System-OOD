package com.auction.client.feature.room;

import com.auction.client.service.AuctionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ChatActionHandlerTest {

    @Test
    @DisplayName("Test hệ thống không gửi tin nhắn rỗng hoặc chỉ chứa toàn dấu cách")
    void testHandle_EmptyChat() {
        AuctionService mockService = mock(AuctionService.class);
        ChatActionHandler handler = new ChatActionHandler(mockService);

        handler.handle(new ChatRequest(null));
        handler.handle(new ChatRequest(""));
        handler.handle(new ChatRequest("    "));

        // Đảm bảo hàm sendChat không bao giờ bị gọi
        verify(mockService, never()).sendChat(anyString());
    }

    @Test
    @DisplayName("Test gửi tin nhắn thành công và tự động xóa dấu cách thừa (trim)")
    void testHandle_ValidChat() {
        AuctionService mockService = mock(AuctionService.class);
        ChatActionHandler handler = new ChatActionHandler(mockService);

        handler.handle(new ChatRequest("   Hello anh em  "));

        // Xác minh tin nhắn được cắt gọn trước khi gửi
        verify(mockService, times(1)).sendChat("Hello anh em");
    }
}
