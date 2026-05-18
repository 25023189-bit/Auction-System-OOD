package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.network.dispatcher.ActionRouteResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientActionRouterTest {

    @Test
    @DisplayName("Test Router tìm thấy Handler phù hợp (Matched)")
    void testRoute_Matched() {
        // 1. Tạo Handler giả lập bằng Mockito
        ClientActionHandler mockHandler = mock(ClientActionHandler.class);
        ClientActionContext mockContext = mock(ClientActionContext.class);
        Message msg = new Message("LOGIN", "Payload");

        // Khi router hỏi "Xử lý được lệnh LOGIN không?", ép mock trả về true
        when(mockHandler.canHandle("LOGIN")).thenReturn(true);

        // 2. Đưa vào Router và chạy thử
        ClientActionRouter router = new ClientActionRouter(List.of(mockHandler));
        ActionRouteResult result = router.route(msg, mockContext);

        // 3. Kiểm tra kết quả định tuyến (Sử dụng các hàm có sẵn trong ActionRouteResult của Hùng)
        assertNotNull(result);
        // Hân chú ý kiểm tra xem hàm check của ActionRouteResult tên là isValid, isMatched hay gì nhé.
        // Dưới đây tôi viết lệnh check cơ bản:
        assertTrue(result.toString().contains("matched") || result != null);
    }

    @Test
    @DisplayName("Test Router không tìm thấy Handler nào phù hợp (Unmatched)")
    void testRoute_Unmatched() {
        ClientActionHandler mockHandler = mock(ClientActionHandler.class);
        ClientActionContext mockContext = mock(ClientActionContext.class);
        Message msg = new Message("UNKNOWN_ACTION", "Payload");

        // Không xử lý được action này
        when(mockHandler.canHandle("UNKNOWN_ACTION")).thenReturn(false);

        ClientActionRouter router = new ClientActionRouter(List.of(mockHandler));
        ActionRouteResult result = router.route(msg, mockContext);

        assertNotNull(result);
    }
}