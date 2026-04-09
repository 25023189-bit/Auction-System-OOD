package com.auction.client.auth;

import com.auction.client.messaging.MessageHandler;
import com.auction.client.navigation.SceneNavigator;
import com.auction.client.role.RolePolicy;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.service.AuctionService;
import com.auction.server.service.ClientConnection;
import javafx.scene.control.Alert;

public class AuthMessageHandler implements MessageHandler {
    private final AuthPresenter presenter;
    private final SceneNavigator navigator;
    private final SessionStore sessionStore;
    private final RolePolicy rolePolicy;
    private final AuctionService auctionService;

    public AuthMessageHandler(AuthPresenter presenter,
                              SceneNavigator navigator,
                              SessionStore sessionStore,
                              RolePolicy rolePolicy,
                              AuctionService auctionService) {
        this.presenter = presenter;
        this.navigator = navigator;
        this.sessionStore = sessionStore;
        this.rolePolicy = rolePolicy;
        this.auctionService = auctionService;
    }

    @Override
    public boolean supports(String action) {
        return switch (action) {
            case "LOGIN_SUCCESS", "LOGIN_FAIL", "REGISTER_SUCCESS", "REGISTER_FAIL", "RESET_SUCCESS", "RESET_FAIL" -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Message message) {
        switch (message.getAction()) {
            case "LOGIN_SUCCESS" -> handleLoginSuccess((User) message.getData());
            case "LOGIN_FAIL" -> presenter.showLoginError(String.valueOf(message.getData()));
            case "REGISTER_SUCCESS" -> {
                presenter.clearLoginPassword();
                navigator.showLogin();
            }
            case "REGISTER_FAIL" -> presenter.showRegisterError(String.valueOf(message.getData()));
            case "RESET_SUCCESS" -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
                alert.setHeaderText("Thành công");
                alert.showAndWait();
                presenter.clearResetForm();
                navigator.showLogin();
            }
            case "RESET_FAIL" -> presenter.showResetError(String.valueOf(message.getData()));
        }
    }

    private void handleLoginSuccess(User user) {
        presenter.showLoginSuccess();

        sessionStore.setCurrentUser(user);
        sessionStore.setCurrentUsername(user.getUsername());
        ClientConnection.currentUser = user.getId();
        auctionService.setCurrentUser(user.getId());

        if (rolePolicy.isAdmin(user)) {
            navigator.openAdminDashboard();
        } else {
            navigator.showLobby();
            auctionService.getRooms();
        }
    }
}