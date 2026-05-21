package com.auction.server.network.dispatcher;

import com.auction.common.dto.Message;
import com.auction.server.handler.ClientActionContext;
import com.auction.server.handler.ClientActionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientActionDispatcherTest {
    private ClientActionDispatcher dispatcher;
    private ClientActionContext mockContext;
    private Message mockMessage;

    @BeforeEach
    void setUp() {
        dispatcher = new ClientActionDispatcher();
        mockContext = mock(ClientActionContext.class);
        mockMessage = new Message("TEST_ACTION", "SENDER", "DATA");
    }

    @Test
    @DisplayName("Test an toàn khi Result bị null")
    void testDispatch_NullResult() {
        assertDoesNotThrow(() -> dispatcher.dispatch(null));
    }

    @Test
    @DisplayName("Test an toàn khi Message hoặc Context bị null")
    void testDispatch_NullMessageOrContext() {
        ClientActionHandler mockHandler = mock(ClientActionHandler.class);

        // Trường hợp Message null
        ActionRouteResult noMessage = ActionRouteResult.matched(mockHandler, null, mockContext);
        assertDoesNotThrow(() -> dispatcher.dispatch(noMessage));

        // Trường hợp Context null
        ActionRouteResult noContext = ActionRouteResult.matched(mockHandler, mockMessage, null);
        assertDoesNotThrow(() -> dispatcher.dispatch(noContext));

        // Đảm bảo handler không bao giờ bị gọi nếu dữ liệu lỗi
        verifyNoInteractions(mockHandler);
    }

    @Test
    @DisplayName("Test trả về lỗi UNKNOWN_ACTION khi không có Handler hỗ trợ")
    void testDispatch_UnmatchedAction() {
        ActionRouteResult unmatchedResult = ActionRouteResult.unmatched(mockMessage, mockContext);

        dispatcher.dispatch(unmatchedResult);

        // Bắt lấy Message được gửi trả về Client để kiểm tra
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext).send(captor.capture());

        Message sentMessage = captor.getValue();
        assertEquals("UNKNOWN_ACTION", sentMessage.getAction());
        assertTrue(sentMessage.getData().toString().contains("Unsupported action"));
    }

    @Test
    @DisplayName("Test gọi Handler thành công khi mọi thứ hợp lệ")
    void testDispatch_Success() {
        ClientActionHandler mockHandler = mock(ClientActionHandler.class);
        ActionRouteResult matchedResult = ActionRouteResult.matched(mockHandler, mockMessage, mockContext);

        dispatcher.dispatch(matchedResult);

        // Xác minh Handler đã được kích hoạt đúng với message và context đó
        verify(mockHandler).handle(mockMessage, mockContext);
    }

    @Test
    @DisplayName("Test bắt lỗi Exception trong Handler và trả về SERVER_ERROR")
    void testDispatch_HandlerThrowsException() {
        ClientActionHandler mockHandler = mock(ClientActionHandler.class);
        ActionRouteResult matchedResult = ActionRouteResult.matched(mockHandler, mockMessage, mockContext);

        // Giả lập Handler gặp lỗi crash hệ thống (NullPointerException, SQLException...)
        doThrow(new RuntimeException("Lỗi mô phỏng")).when(mockHandler).handle(any(), any());

        dispatcher.dispatch(matchedResult);

        // Kiểm tra xem hệ thống có bắt lỗi và gửi thông báo SERVER_ERROR về cho Client không
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext).send(captor.capture());

        Message sentMessage = captor.getValue();
        assertEquals("SERVER_ERROR", sentMessage.getAction());
    }
}