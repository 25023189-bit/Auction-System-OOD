package com.auction.client.feature.room;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.controllers.auction.autobid.AutoBidController;
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
        assertFalse(handler.supports("ROOM_JOINED"));

        assertTrue(handler.supports("ROOM_STATE_UPDATED"));
        assertTrue(handler.supports("BID_SUCCESS"));
        assertTrue(handler.supports("BID_SUCCESS_EXTENDED"));
        assertTrue(handler.supports("AUTO_BID_SET_SUCCESS"));
        assertTrue(handler.supports("AUTO_BID_SET_FAILED"));
        assertTrue(handler.supports("AUTO_BID_CANCEL_SUCCESS"));
        assertTrue(handler.supports("AUTO_BID_CANCEL_FAILED"));
        assertTrue(handler.supports("CHAT_MSG"));
        assertTrue(handler.supports("UPDATE_PRICE"));

        assertFalse(handler.supports("UNKNOWN_ACTION"));
    }

    @Test
    void testHandle_RoomJoined_DoesNothingBecauseHandledByFlowFallback() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("ROOM_001");

        Message message = new Message("ROOM_JOINED", room);

        handler.handle(message);

        verify(mockSession, never()).setCurrentRoom(any(AuctionRoom.class));
        verify(mockSession, never()).setCurrentRoomId(anyString());
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

    @Test
    void testHandle_AutoBidResponses() {
        AutoBidController autoBidController = mock(AutoBidController.class);
        AuctionRoomMessageHandler autoBidHandler = new AuctionRoomMessageHandler(
                mockSession, mockNavigator, mockBinder, mockPresenter, mockTimer, autoBidController
        );

        autoBidHandler.handle(new Message("AUTO_BID_SET_SUCCESS", "SERVER", "ok"));
        autoBidHandler.handle(new Message("AUTO_BID_SET_FAILED", "SERVER", "bad"));
        autoBidHandler.handle(new Message("AUTO_BID_CANCEL_SUCCESS", "SERVER", "ok"));
        autoBidHandler.handle(new Message("AUTO_BID_CANCEL_FAILED", "SERVER", "bad"));

        verify(autoBidController).handleSetSuccess();
        verify(autoBidController).handleSetFailed("bad");
        verify(autoBidController).handleCancelSuccess();
        verify(autoBidController).handleCancelFailed("bad");
    }
}
