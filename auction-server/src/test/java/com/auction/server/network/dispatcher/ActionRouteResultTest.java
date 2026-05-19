package com.auction.server.network.dispatcher;

import com.auction.common.dto.Message;
import com.auction.server.handler.ClientActionContext;
import com.auction.server.handler.ClientActionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ActionRouteResultTest {

    @Test
    @DisplayName("Test tạo kết quả Matched (Đã tìm thấy Handler)")
    void testMatched() {
        ClientActionHandler mockHandler = mock(ClientActionHandler.class);
        Message mockMessage = new Message("LOGIN", "USER", "DATA");
        ClientActionContext mockContext = mock(ClientActionContext.class);

        ActionRouteResult result = ActionRouteResult.matched(mockHandler, mockMessage, mockContext);

        assertTrue(result.hasHandler());
        assertEquals(mockHandler, result.getHandler());
        assertEquals(mockMessage, result.getMessage());
        assertEquals(mockContext, result.getContext());
    }

    @Test
    @DisplayName("Test tạo kết quả Unmatched (Không tìm thấy Handler)")
    void testUnmatched() {
        Message mockMessage = new Message("UNKNOWN", "USER", "DATA");
        ClientActionContext mockContext = mock(ClientActionContext.class);

        ActionRouteResult result = ActionRouteResult.unmatched(mockMessage, mockContext);

        assertFalse(result.hasHandler());
        assertNull(result.getHandler());
        assertEquals(mockMessage, result.getMessage());
        assertEquals(mockContext, result.getContext());
    }
}