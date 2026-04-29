package com.auction.client.network.messaging;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.server.service.AuctionService;
import com.auction.server.service.ClientConnection;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý thông báo tài khoản bị khóa từ server.
 * Client phải xóa session, quay về login và đóng các cửa sổ phụ.
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
        ClientConnection.currentUser = null;
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
