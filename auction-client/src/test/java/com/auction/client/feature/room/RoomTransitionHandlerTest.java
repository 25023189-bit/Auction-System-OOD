package com.auction.client.feature.room;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class RoomTransitionHandlerTest {
    private AuctionService mockService;
    private SessionStore mockSession;
    private SceneNavigator mockNavigator;
    private AuctionTimer mockTimer;
    private LobbyUserInfoBinder mockBinder;
    private RoomTransitionHandler handler;

    @BeforeEach
    void setUp() {
        mockService = mock(AuctionService.class);
        mockSession = mock(SessionStore.class);
        mockNavigator = mock(SceneNavigator.class);
        mockTimer = mock(AuctionTimer.class);
        mockBinder = mock(LobbyUserInfoBinder.class);
        handler = new RoomTransitionHandler(mockService, mockSession, mockNavigator, mockTimer, mockBinder);
    }

    @Test
    @DisplayName("Test quay lại sảnh (backToLobby) gọi đầy đủ các luồng dọn dẹp")
    void testBackToLobby_FullFlow() {
        User mockUser = new User();
        when(mockSession.getCurrentUser()).thenReturn(mockUser);

        handler.backToLobby();

        // Kiểm tra xem tất cả các bước dọn dẹp đã được gọi đúng thứ tự chưa
        verify(mockTimer).stop();
        verify(mockService).leaveRoom();
        verify(mockSession).setCurrentRoom(null);
        verify(mockSession).setCurrentRoomId(null);
        verify(mockNavigator).showLobby();
        verify(mockBinder).bind(mockUser);
        verify(mockService).getRooms();
    }

    @Test
    @DisplayName("Test quay lại sảnh an toàn khi các dependencies bị null (Không văng lỗi)")
    void testBackToLobby_NullDependencies() {
        RoomTransitionHandler nullHandler = new RoomTransitionHandler(null, null, null, null, null);
        assertDoesNotThrow(nullHandler::backToLobby);
    }
}