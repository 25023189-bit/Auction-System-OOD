package com.auction.client.feature.room;

import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DefaultAuctionTimerServiceTest {
    private AuctionRoomPresenter mockPresenter;
    private DefaultAuctionTimerService timerService;

    @BeforeEach
    void setUp() {
        mockPresenter = mock(AuctionRoomPresenter.class);
        timerService = new DefaultAuctionTimerService(mockPresenter);
    }

    @Test
    @DisplayName("Test Start với thời gian null -> Báo không có giới hạn")
    void testStart_NullTimes() {
        timerService.start(null, null);
        verify(mockPresenter).setTimerText("No time limit", Color.ORANGE);
    }

    @Test
    @DisplayName("Test Start khi phiên đấu giá đã kết thúc trong quá khứ")
    void testStart_AlreadyEnded() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        timerService.start(pastTime, pastTime);

        verify(mockPresenter).setTimerText("Ended", Color.RED);
        verify(mockPresenter).disableBidUi("Auction has ended.");
    }

    @Test
    @DisplayName("Test Start khi phiên đấu giá chưa bắt đầu (Đang chờ)")
    void testStart_BeforeStart() {
        LocalDateTime futureStart = LocalDateTime.now().plusHours(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusHours(2);

        // [UI ẢO]: Chặn việc khởi tạo Timeline thật để tránh lỗi Toolkit Not Initialized
        try (MockedConstruction<Timeline> mockedTimeline = mockConstruction(Timeline.class)) {
            timerService.start(futureStart, futureEnd);
            verify(mockPresenter).setTimerText(anyString(), eq(Color.BLUE));
        }
    }

    @Test
    @DisplayName("Test Start khi phiên đấu giá đang diễn ra (Đếm ngược)")
    void testStart_Running() {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusHours(1);

        try (MockedConstruction<Timeline> mockedTimeline = mockConstruction(Timeline.class)) {
            timerService.start(pastStart, futureEnd);
            verify(mockPresenter, atLeastOnce()).setTimerText(anyString(), eq(Color.DARKGREEN));
        }
    }

    @Test
    @DisplayName("Test Stop an toàn không bị sập")
    void testStop() {
        try (MockedConstruction<Timeline> mockedTimeline = mockConstruction(Timeline.class)) {
            timerService.start(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

            // Gọi stop lần 1
            assertDoesNotThrow(() -> timerService.stop());
            // Gọi stop lần 2 khi timeline đã null
            assertDoesNotThrow(() -> timerService.stop());
        }
    }
}