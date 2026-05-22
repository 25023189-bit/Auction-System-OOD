package com.auction.client.network.messaging;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.feature.room.AuctionRoomPresenter;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionFlowFallbackHandlerTest {

    // === BẬT ĐỘNG CƠ JAVAFX NGẦM ===
    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    private AuctionService mockAuctionService;
    private SessionStore mockSessionStore;
    private SceneNavigator mockSceneNavigator;
    private LobbyUserInfoBinder mockLobbyUserInfoBinder;
    private AuctionRoomPresenter mockAuctionRoomPresenter;
    private AuctionFlowFallbackHandler handler;

    @BeforeEach
    void setUp() {
        mockAuctionService = mock(AuctionService.class);
        mockSessionStore = mock(SessionStore.class);
        mockSceneNavigator = mock(SceneNavigator.class);
        mockLobbyUserInfoBinder = mock(LobbyUserInfoBinder.class);
        mockAuctionRoomPresenter = mock(AuctionRoomPresenter.class);

        handler = new AuctionFlowFallbackHandler(
                mockAuctionService, mockSessionStore, mockSceneNavigator,
                mockLobbyUserInfoBinder, mockAuctionRoomPresenter
        );
    }

    // ==========================================
    // NHÓM 1: TEST HÀM SUPPORTS
    // ==========================================
    @Test
    @DisplayName("Hàm supports: Phải chấp nhận 10 action quy định")
    void testSupports_ValidActions_ReturnsTrue() {
        assertTrue(handler.supports("CREATE_AUCTION_SUCCESS"));
        assertTrue(handler.supports("CREATE_AUCTION_PENDING"));
        assertTrue(handler.supports("CREATE_AUCTION_FAIL"));
        assertTrue(handler.supports("UPDATE_ROOMS"));
        assertTrue(handler.supports("BID_FAIL"));
        assertTrue(handler.supports("ROOM_FAIL"));
        assertTrue(handler.supports("BID_SUCCESS"));
        assertTrue(handler.supports("CLOSE_AUCTION_SUCCESS"));
        assertTrue(handler.supports("CLOSE_AUCTION_FAIL"));
        assertTrue(handler.supports("AUCTION_CLOSED_NOTIFY"));
    }

    @Test
    @DisplayName("Hàm supports: Phải từ chối action lạ")
    void testSupports_InvalidActions_ReturnsFalse() {
        assertFalse(handler.supports("UNKNOWN_ACTION"));
        assertFalse(handler.supports(null));
    }

    // ==========================================
    // NHÓM 2: TEST HÀM HANDLE (KHÔNG CÓ ALERT)
    // ==========================================
    @Test
    @DisplayName("Nhánh CREATE_AUCTION_SUCCESS: Cập nhật Session và Join phòng")
    void testHandle_CreateAuctionSuccess() {
        Message msg = new Message("CREATE_AUCTION_SUCCESS", "ROOM_101", null);
        handler.handle(msg);

        verify(mockSessionStore).setCurrentRoomId("ROOM_101");
        verify(mockAuctionService).joinRoom("ROOM_101");
    }

    @Test
    @DisplayName("Nhánh UPDATE_ROOMS: Gọi API tải lại danh sách phòng")
    void testHandle_UpdateRooms() {
        Message msg = new Message("UPDATE_ROOMS", "SERVER", null);
        handler.handle(msg);
        verify(mockAuctionService).getRooms();
    }

    @Test
    @DisplayName("Nhánh BID_FAIL: Nối lỗi vào Chat (Nếu Presenter tồn tại)")
    void testHandle_BidFail() {
        Message msg = new Message("BID_FAIL", "SERVER", "Not enough money");
        handler.handle(msg);
        verify(mockAuctionRoomPresenter).appendChat("Error: Not enough money");
    }

    @Test
    @DisplayName("Nhánh BID_SUCCESS: Cập nhật giá mới (Bỏ qua nếu dữ liệu không phải Double)")
    void testHandle_BidSuccess() {
        // Trường hợp 1: Dữ liệu chuẩn Double
        Message validMsg = new Message("BID_SUCCESS", "SERVER", 500.0);
        handler.handle(validMsg);
        verify(mockAuctionRoomPresenter).showCurrentPrice(500.0, null);

        // Trường hợp 2: Dữ liệu sai kiểu (String) -> Hệ thống phải an toàn bỏ qua
        Message invalidMsg = new Message("BID_SUCCESS", "SERVER", "500.0");
        handler.handle(invalidMsg);
        // Không được gọi thêm lần nào nữa (times = 1 từ lệnh trên)
        verify(mockAuctionRoomPresenter, times(1)).showCurrentPrice(anyDouble(), any());
    }

    // ==========================================
    // NHÓM 3: TEST HÀM HANDLE (BẬT ALERT & ĐIỀU HƯỚNG)
    // ==========================================
    @Test
    @DisplayName("Nhánh CREATE_AUCTION_PENDING & FAIL: Hiển thị Alert thông báo")
    void testHandle_CreateAuctionPendingAndFail() {
        try (MockedConstruction<Alert> mockedAlerts = mockConstruction(Alert.class)) {
            // Test Pending
            handler.handle(new Message("CREATE_AUCTION_PENDING", "SERVER", "Waiting..."));
            // Test Fail
            handler.handle(new Message("CREATE_AUCTION_FAIL", "SERVER", "Invalid data"));

            assertEquals(2, mockedAlerts.constructed().size());

            // Xác thực Alert 1 (Pending)
            verify(mockedAlerts.constructed().get(0)).setHeaderText("Auction Waiting For Approval");
            verify(mockedAlerts.constructed().get(0)).showAndWait();

            // Xác thực Alert 2 (Fail)
            verify(mockedAlerts.constructed().get(1)).setHeaderText("Unable to Create Auction");
            verify(mockedAlerts.constructed().get(1)).showAndWait();
        }
    }

    @Test
    @DisplayName("Nhánh ROOM_FAIL: Bật Alert, đá về Lobby và tải lại danh sách phòng")
    void testHandle_RoomFail() {
        User mockUser = mock(User.class);
        when(mockSessionStore.getCurrentUser()).thenReturn(mockUser);

        try (MockedConstruction<Alert> mockedAlerts = mockConstruction(Alert.class)) {
            Message msg = new Message("ROOM_FAIL", "SERVER", "Room is full");
            handler.handle(msg);

            assertEquals(1, mockedAlerts.constructed().size());
            verify(mockedAlerts.constructed().get(0)).setHeaderText("Unable to Join Room");

            // Xác thực dọn dẹp Session
            verify(mockSessionStore).setCurrentRoom(null);
            verify(mockSessionStore).setCurrentRoomId(null);

            // Xác thực đá về Lobby
            verify(mockSceneNavigator).showLobby();
            verify(mockLobbyUserInfoBinder).bind(mockUser);
            verify(mockAuctionService).getRooms();
        }
    }

    @Test
    @DisplayName("Nhánh AUCTION_CLOSED_NOTIFY: Đang ở trong phòng bị đóng -> Đá về Lobby")
    void testHandle_AuctionClosedNotify_InTargetRoom() {
        when(mockSessionStore.getCurrentRoomId()).thenReturn("ROOM_999");

        try (MockedConstruction<Alert> mockedAlerts = mockConstruction(Alert.class)) {
            // Server báo đóng phòng 999 (Trùng với phòng hiện tại)
            Message msg = new Message("AUCTION_CLOSED_NOTIFY", "SERVER", "ROOM_999");
            handler.handle(msg);

            assertEquals(1, mockedAlerts.constructed().size(), "Phải bật Alert cảnh báo");
            verify(mockSessionStore).setCurrentRoom(null);
            verify(mockSceneNavigator).showLobby();
        }
    }

    @Test
    @DisplayName("Nhánh AUCTION_CLOSED_NOTIFY: Đang ở phòng khác -> Không làm gì cả")
    void testHandle_AuctionClosedNotify_InDifferentRoom() {
        when(mockSessionStore.getCurrentRoomId()).thenReturn("ROOM_123");

        try (MockedConstruction<Alert> mockedAlerts = mockConstruction(Alert.class)) {
            // Server báo đóng phòng 999 (Không liên quan đến mình)
            Message msg = new Message("AUCTION_CLOSED_NOTIFY", "SERVER", "ROOM_999");
            handler.handle(msg);

            // Xác thực hoàn toàn im lặng, không bật Alert, không bị văng ra Lobby
            assertEquals(0, mockedAlerts.constructed().size());
            verify(mockSceneNavigator, never()).showLobby();
        }
    }
}