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

    @Test
    void roomStateUpdateBindsOnlyCurrentRoomAndStartsTimer() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("ROOM_ACTIVE");
        room.setCurrentPrice(320.0);
        room.setStartTime(LocalDateTime.now().minusMinutes(1));
        room.setEndTime(LocalDateTime.now().plusMinutes(4));
        room.setScheduledEndTime(LocalDateTime.now().plusMinutes(5));
        when(mockSession.getCurrentRoomId()).thenReturn("ROOM_ACTIVE");

        handler.handle(new Message("ROOM_STATE_UPDATED", "SERVER", room));

        verify(mockSession).setCurrentRoom(room);
        verify(mockBinder).bind(room);
        verify(mockPresenter).showCurrentPrice(320.0, null);
        verify(mockTimer).start(room.getStartTime(), room.getScheduledEndTime());

        reset(mockBinder);
        when(mockSession.getCurrentRoomId()).thenReturn("OTHER");
        handler.handle(new Message("ROOM_STATE_UPDATED", "SERVER", room));
        handler.handle(new Message("ROOM_STATE_UPDATED", "SERVER", "bad payload"));
        verifyNoInteractions(mockBinder);
    }

    @Test
    void bidSuccessUsesBidderNameAndExplainsExtension() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("ROOM_ACTIVE");
        room.setCurrentPrice(410.0);
        room.setStartTime(LocalDateTime.now().minusMinutes(1));
        room.setEndTime(LocalDateTime.now().plusMinutes(1));
        when(mockSession.getCurrentRoomId()).thenReturn("ROOM_ACTIVE");

        handler.handle(new Message("BID_SUCCESS_EXTENDED", "newBidder", room));

        verify(mockSession).setCurrentRoom(room);
        verify(mockBinder).bind(room);
        verify(mockPresenter).showCurrentPrice(410.0, "newBidder");
        verify(mockPresenter).appendChat("Auction extended because a bid was placed in the final 30 seconds.");
        verify(mockTimer).start(room.getStartTime(), room.getEndTime());

        handler.handle(new Message("BID_SUCCESS", "SERVER", "no room"));
        verify(mockPresenter).appendChat("New bid received!");
    }

    @Test
    void updatePriceIgnoresMalformedOrOtherRoomPayloads() {
        AuctionRoom currentRoom = new AuctionRoom();
        when(mockSession.getCurrentRoomId()).thenReturn("ROOM_ACTIVE");
        when(mockSession.getCurrentRoom()).thenReturn(currentRoom);

        handler.handle(new Message("UPDATE_PRICE", "SERVER", null));
        handler.handle(new Message("UPDATE_PRICE", "SERVER", "invalid"));
        handler.handle(new Message("UPDATE_PRICE", "SERVER", "OTHER|250"));
        handler.handle(new Message("UPDATE_PRICE", "SERVER", "ROOM_ACTIVE|bad"));

        verify(mockPresenter, never()).showCurrentPrice(anyDouble(), any());
        assertEquals(0.0, currentRoom.getCurrentPrice());
    }
}
