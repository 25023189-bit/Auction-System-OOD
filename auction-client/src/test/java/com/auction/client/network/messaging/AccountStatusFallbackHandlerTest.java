package com.auction.client.network.messaging;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountStatusFallbackHandlerTest {

    // === BẬT ĐỘNG CƠ JAVAFX NGẦM ===
    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    private SceneNavigator mockSceneNavigator;
    private SessionStore mockSessionStore;
    private AuctionService mockAuctionService;
    private AccountStatusFallbackHandler handler;

    @BeforeEach
    void setUp() {
        mockSceneNavigator = mock(SceneNavigator.class);
        mockSessionStore = mock(SessionStore.class);
        mockAuctionService = mock(AuctionService.class);

        handler = new AccountStatusFallbackHandler(
                mockSceneNavigator, mockSessionStore, mockAuctionService
        );
    }

    @Test
    @DisplayName("Hàm supports: Chỉ chấp nhận Action là BANNED")
    void testSupports_OnlyAcceptsBannedAction() {
        assertTrue(handler.supports("BANNED"));
        assertFalse(handler.supports("LOGIN_SUCCESS"));
        assertFalse(handler.supports(""));
        assertFalse(handler.supports(null));
    }

    @Test
    @DisplayName("Hàm handle: Xử lý đuổi User, hiện cảnh báo, xóa Session và đóng cửa sổ phụ")
    void testHandle_BansUserAndCleansUpUI() {
        // 1. Chuẩn bị thông điệp "Án tử" từ Server
        Message mockMessage = new Message("BANNED", "SERVER", "Phát hiện gian lận");

        // 2. Chuẩn bị các cửa sổ giả lập trên màn hình
        Stage mainStage = mock(Stage.class);
        when(mainStage.getTitle()).thenReturn("Auction System"); // Cửa sổ chính

        Stage popupStage = mock(Stage.class);
        when(popupStage.getTitle()).thenReturn("Chi tiết sản phẩm"); // Cửa sổ phụ

        Window genericWindow = mock(Window.class); // Một cửa sổ ẩn danh nào đó không phải Stage

        ObservableList<Window> mockWindows = FXCollections.observableArrayList(
                mainStage, popupStage, genericWindow
        );

        // 3. SONG KIẾM HỢP BÍCH: Đóng băng cả Alert lẫn class Window
        try (MockedConstruction<Alert> mockedAlerts = mockConstruction(Alert.class);
             MockedStatic<Window> mockedStaticWindow = mockStatic(Window.class)) {

            // Báo cho hệ thống ảo biết danh sách cửa sổ hiện tại
            mockedStaticWindow.when(Window::getWindows).thenReturn(mockWindows);

            // 4. Kích hoạt bóp cò
            handler.handle(mockMessage);

            // === NGHIỆM THU ===

            // 5. Xác thực Alert đã được bật với đúng nội dung
            assertEquals(1, mockedAlerts.constructed().size(), "Phải có 1 Alert được bật lên");
            Alert mockAlert = mockedAlerts.constructed().get(0);
            verify(mockAlert).setTitle("System Notification");
            verify(mockAlert).setHeaderText("ACCOUNT DISABLED");
            verify(mockAlert).setContentText("Phát hiện gian lận");
            verify(mockAlert).showAndWait();

            // 6. Xác thực dữ liệu Session và Service đã bị "tẩy trắng"
            verify(mockSessionStore, times(1)).clearSession();
            verify(mockAuctionService, times(1)).setCurrentUser(null);

            // 7. Xác thực đã đá về màn hình Login
            verify(mockSceneNavigator, times(1)).showLogin();

            // 8. Xác thực Cửa sổ chính KHÔNG BỊ ĐÓNG, Cửa sổ phụ BỊ ĐÓNG
            verify(mainStage, never()).close();
            verify(popupStage, times(1)).close();
            // (genericWindow không phải là Stage nên vòng lặp if (window instanceof Stage) sẽ bỏ qua nó)
        }
    }
}