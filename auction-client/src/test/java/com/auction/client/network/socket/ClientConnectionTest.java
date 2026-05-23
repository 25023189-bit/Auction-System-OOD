package com.auction.client.network.socket;

import com.auction.common.dto.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ClientConnectionTest {

    @Test
    @DisplayName("Test gửi Message an toàn khi stream bị null")
    void testSendMessage_NullStream() {
        ServerMessageListener mockListener = mock(ServerMessageListener.class);
        ClientConnection connection = new ClientConnection(mockListener);

        // Gửi Message khi chưa connect không được sập chương trình
        // Đã sửa lại constructor cho khớp với class Message của bạn
        assertDoesNotThrow(() -> connection.sendMessage(new Message("TEST_ACTION", "CLIENT", "DATA")));
    }

    @Test
    @DisplayName("Test đóng kết nối an toàn khi các luồng chưa khởi tạo")
    void testCloseConnection_SafeClosure() {
        ServerMessageListener mockListener = mock(ServerMessageListener.class);
        ClientConnection connection = new ClientConnection(mockListener);

        // Gọi close khi chưa connect không được sinh lỗi
        assertDoesNotThrow(connection::closeConnection);
    }

    @Test
    @DisplayName("Test bắt lỗi khi kết nối Socket thất bại")
    void testConnect_ExceptionHandling() {
        ServerMessageListener mockListener = mock(ServerMessageListener.class);
        ClientConnection connection = new ClientConnection(mockListener);

        // Giả lập Socket tạo ra bị lỗi để nhảy vào khối Catch
        try (MockedConstruction<Socket> socketMock = mockConstruction(Socket.class, (mock, context) -> {
            when(mock.getInputStream()).thenThrow(new RuntimeException("Mạng bị ngắt kết nối ảo"));
        })) {
            connection.connect();

            // Đợi thread nền chạy 1 chút
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}

            verify(mockListener).updateConnectionStatus("Connecting...");
        }
    }
}
