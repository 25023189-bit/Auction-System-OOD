package com.auction.client.network.messaging;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.client.service.AuctionService;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback handler xử lý thông báo tài khoản bị khóa từ server.
 *
 * Vai trò:
 * - Hiển thị thông báo BANNED cho người dùng.
 * - Xóa session, reset currentUser, quay về login và đóng các cửa sổ phụ.
 *
 * Luồng chính:
 * 1. FallbackMessageHandler chuyển action BANNED vào handler này.
 * 2. Handler show alert, clear session/service user, gọi showLogin() và đóng dashboard/popup phụ.
 *
 * Business rules:
 * - User bị xóa/khóa không được tiếp tục thao tác trên dashboard hoặc popup đang mở.
 * - Cửa sổ chính Auction System được giữ lại, các Stage phụ bị đóng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: Alert, Stage và Window JavaFX phải thao tác trên JavaFX Application Thread.
 * - Dependency: MessageHandler, SceneNavigator, SessionStore, AuctionService, ClientConnection, Alert, Stage/Window.
 */
public class AccountStatusFallbackHandler implements MessageHandler {
    private final SceneNavigator sceneNavigator;
    private final SessionStore sessionStore;
    private final AuctionService auctionService;

    public AccountStatusFallbackHandler(SceneNavigator sceneNavigator,
                                        SessionStore sessionStore,
                                        AuctionService auctionService) {
        this.sceneNavigator = sceneNavigator;
        this.sessionStore = sessionStore;
        this.auctionService = auctionService;
    }

    @Override
    public boolean supports(String action) {
        return "BANNED".equals(action);
    }

    @Override
    public void handle(Message msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Notification");
        alert.setHeaderText("ACCOUNT DISABLED");
        alert.setContentText(String.valueOf(msg.getData()));
        alert.showAndWait();

        // Xóa cả session cục bộ lẫn user đang lưu trong service/socket.
        sessionStore.clearSession();
        auctionService.setCurrentUser(null);

        sceneNavigator.showLogin();

        // Đóng dashboard/popup phụ để user bị khóa không tiếp tục thao tác.
        List<Window> openWindows = new ArrayList<>(Window.getWindows());
        for (Window window : openWindows) {
            if (window instanceof Stage stage) {
                if (!"Auction System".equals(stage.getTitle())) {
                    stage.close();
                }
            }
        }
    }
}
