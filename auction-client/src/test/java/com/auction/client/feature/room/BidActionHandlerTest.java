package com.auction.client.feature.room;

import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class BidActionHandlerTest {
    private AuctionService mockService;
    private SessionStore mockSession;
    private AuctionRoomPresenter mockPresenter;
    private BidActionHandler handler;

    @BeforeEach
    void setUp() {
        mockService = mock(AuctionService.class);
        mockSession = mock(SessionStore.class);
        mockPresenter = mock(AuctionRoomPresenter.class);
        handler = new BidActionHandler(mockService, mockSession, mockPresenter);
    }

    @Test
    @DisplayName("Test báo lỗi khi người dùng gõ chữ vào ô nhập tiền (Không phải số)")
    void testHandle_InvalidNumber() {
        handler.handle(new BidRequest("muời nghìn"));
        verify(mockPresenter).appendChat("System: Please enter a valid amount.");
        verify(mockService, never()).placeBid(anyDouble());
    }

    @Test
    @DisplayName("Test chặn đặt giá khi phiên đấu giá chưa tới giờ bắt đầu")
    void testHandle_NotStartedYet() {
        AuctionRoom room = new AuctionRoom();
        // Đặt thời gian bắt đầu là ngày mai
        room.setStartTime(LocalDateTime.now().plusDays(1));
        when(mockSession.getCurrentRoom()).thenReturn(room);

        handler.handle(new BidRequest("1000"));

        verify(mockPresenter).appendChat("Auction has not started yet. Bidding is not available.");
        verify(mockService, never()).placeBid(anyDouble());
    }

    @Test
    @DisplayName("Test chặn đặt giá khi phiên đấu giá đã quá hạn")
    void testHandle_AuctionEnded() {
        AuctionRoom room = new AuctionRoom();
        // Đặt thời gian kết thúc là ngày hôm qua
        room.setStartTime(LocalDateTime.now().minusDays(2));
        room.setScheduledEndTime(LocalDateTime.now().minusDays(1));
        when(mockSession.getCurrentRoom()).thenReturn(room);

        handler.handle(new BidRequest("1000"));

        verify(mockPresenter).disableBidUi("Auction has ended.");
        verify(mockService, never()).placeBid(anyDouble());
    }

    @Test
    @DisplayName("Test đặt giá thành công khi phòng đang mở cửa")
    void testHandle_Success() {
        AuctionRoom room = new AuctionRoom();
        room.setStartTime(LocalDateTime.now().minusHours(1));
        room.setScheduledEndTime(LocalDateTime.now().plusHours(1));
        when(mockSession.getCurrentRoom()).thenReturn(room);

        handler.handle(new BidRequest("5000"));

        // Xác minh ClientService đã được gọi để bắn dữ liệu xuống Socket
        verify(mockService).placeBid(5000.0);
    }
}