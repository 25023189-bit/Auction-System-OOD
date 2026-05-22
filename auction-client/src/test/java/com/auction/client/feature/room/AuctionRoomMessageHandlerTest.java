package com.auction.client.feature.room;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionRoomMessageHandlerTest {
    private SessionStore mockSession;
    private SceneNavigator mockNavigator;
    private AuctionRoomStateBinder mockBinder;
    private AuctionRoomPresenter mockPresenter;
    private AuctionTimer mockTimer;
    private AuctionRoomMessageHandler handler;

    @BeforeEach
    void setUp() {
        mockSession = mock(SessionStore.class);
        mockNavigator = mock(SceneNavigator.class);
        mockBinder = mock(AuctionRoomStateBinder.class);
        mockPresenter = mock(AuctionRoomPresenter.class);
        mockTimer = mock(AuctionTimer.class);
        handler = new AuctionRoomMessageHandler(mockSession, mockNavigator, mockBinder, mockPresenter, mockTimer);
    }

    @Test
    @DisplayName("Test bộ lọc supports() nhận đúng các sự kiện phòng đấu giá")
    void testSupports() {
        assertTrue(handler.supports("ROOM_JOINED"));
        assertTrue(handler.supports("BID_SUCCESS"));
        assertTrue(handler.supports("UPDATE_PRICE"));
        assertFalse(handler.supports("LOGIN_SUCCESS")); // Sự kiện lạ sẽ bị từ chối
    }

    @Test
    @DisplayName("Test xử lý ROOM_JOINED: Lưu session và bật Timer")
    void testHandle_RoomJoined() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("AU999");
        room.setStartTime(LocalDateTime.now());
        Message msg = new Message("ROOM_JOINED", "SERVER", room);

        handler.handle(msg);

        verify(mockSession).setCurrentRoom(room);
        verify(mockSession).setCurrentRoomId("AU999");
        verify(mockNavigator).showAuctionRoom(room);
        verify(mockBinder).bind(room);
        verify(mockTimer).start(any(), any());
    }

    @Test
    @DisplayName("Test xử lý CHAT_MSG: Ghi tin nhắn ra màn hình")
    void testHandle_ChatMsg() {
        Message msg = new Message("CHAT_MSG", "hanto", "Xin chào mọi người");
        // Giả lập các thuộc tính public của Message (nếu có theo code gốc)
        msg.username = "hanto";
        msg.data = "Xin chào mọi người";

        handler.handle(msg);

        verify(mockPresenter).appendChat("[hanto]: Xin chào mọi người");
    }

    @Test
    @DisplayName("Test xử lý UPDATE_PRICE: Cắt chuỗi và cập nhật giá mới")
    void testHandle_UpdatePrice() {
        // Chuỗi data trả về dạng: "Mã_Phòng|Giá_Mới"
        Message msg = new Message("UPDATE_PRICE", "SERVER", "AU999|7500.0");

        when(mockSession.getCurrentRoomId()).thenReturn("AU999");
        AuctionRoom currentRoom = new AuctionRoom();
        when(mockSession.getCurrentRoom()).thenReturn(currentRoom);

        handler.handle(msg);

        assertEquals(7500.0, currentRoom.getCurrentPrice());
        verify(mockPresenter).showCurrentPrice(7500.0, null);
    }
}
