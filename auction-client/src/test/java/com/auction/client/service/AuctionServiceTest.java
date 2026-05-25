package com.auction.client.service;

import com.auction.client.network.socket.ClientConnection;
import com.auction.common.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionServiceTest {
    private ClientConnection mockConnection;
    private AuctionService service;

    @BeforeEach
    void setUp() {
        mockConnection = mock(ClientConnection.class);
        service = new AuctionService(mockConnection);
        service.setCurrentUser("USER123");
    }

    @Test
    @DisplayName("Test cập nhật User và Connection")
    void testUserAndConnection() {
        service.setCurrentUser("NEW_USER");
        assertEquals("NEW_USER", service.getCurrentUser());
        assertEquals(mockConnection, service.getClientConnection());
    }

    @Test
    @DisplayName("Test các hàm gọi lệnh hệ thống và phòng đấu giá")
    void testRoomAndSystemActions() {
        service.getRooms();
        verifyActionSent("GET_ROOMS");

        service.login("user", "pass");
        verifyActionSent("LOGIN");

        service.register("C1", "user", "name", "pass", "BIDDER", null);
        verifyActionSent("REGISTER");

        service.joinRoom("R1");
        verifyActionSent("JOIN_ROOM");

        service.leaveRoom();
        verifyActionSent("LEAVE_ROOM");

        service.placeBid(500.0);
        verifyActionSent("BID");

        service.closeAuction("R1");
        verifyActionSent("CLOSE_AUCTION");

        service.sendChat("Hello");
        verifyActionSent("CHAT_MSG");

        service.getBidHistory("R1");
        verifyActionSent("GET_BID_HISTORY");
    }

    @Test
    @DisplayName("Test quên và đổi mật khẩu")
    void testPasswordActions() {
        service.resetPassword("user", "newPass", "newPass");
        verifyActionSent("RESET_PASSWORD");

    }

    @Test
    @DisplayName("Login sends username as the message id")
    void testLoginUsesUsernameMessageId() {
        service.login("hanto", "pass");

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(mockConnection).sendMessage(captor.capture());

        Message message = captor.getValue();
        assertEquals("LOGIN", message.getAction());
        assertEquals("hanto", message.getId());
        assertEquals("pass", message.getData());
    }

    @Test
    @DisplayName("Register payload keeps username before full name")
    void testRegisterPayloadKeepsUsernameBeforeFullName() {
        service.register("BD50001", "hanto", "To Bao Han", "pass", "BIDDER", null);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(mockConnection).sendMessage(captor.capture());

        Message message = captor.getValue();
        assertEquals("REGISTER", message.getAction());
        assertEquals("BD50001|hanto|To Bao Han|pass|BIDDER|", message.getData());
    }

    @Test
    @DisplayName("Test tạo phiên đấu giá (Create Auction)")
    void testCreateAuction() {
        service.createAuction("Item", "Desc", 100, 10, 5, LocalDateTime.now(), 60, 30);
        verifyActionSent("CREATE_AUCTION");
    }

    @Test
    @DisplayName("Test ngắt kết nối an toàn")
    void testDisconnect() {
        service.disconnect();
        verify(mockConnection).closeConnection();
        assertEquals("", service.getCurrentUser());
    }

    @Test
    @DisplayName("Test Product Details Callback trả dữ liệu về giao diện")
    void testProductDetails() {
        service.requestProductDetails("R1");
        verifyActionSent("GET_PRODUCT_DETAILS");

        AtomicBoolean callbackTriggered = new AtomicBoolean(false);
        service.setProductDetailsCallback(data -> callbackTriggered.set(true));
        service.fireProductDetailsReceived("MockData");
        assertTrue(callbackTriggered.get());
    }

    private void verifyActionSent(String expectedAction) {
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(mockConnection, atLeastOnce()).sendMessage(captor.capture());
        boolean found = captor.getAllValues().stream()
                .anyMatch(msg -> msg.getAction().equals(expectedAction));
        assertTrue(found, "Không tìm thấy Message với action: " + expectedAction);
    }
}
