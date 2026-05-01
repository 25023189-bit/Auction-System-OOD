package com.auction.client.feature.auth;

import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.common.role.RolePolicy;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.service.AuctionService;
import com.auction.server.service.ClientConnection;
import javafx.scene.control.Alert;

/**
 * MessageHandler xử lý phản hồi server cho nhóm xác thực.
 *
 * Vai trò:
 * - Nhận LOGIN/REGISTER/RESET response và cập nhật presenter tương ứng.
 * - Lưu session sau login thành công rồi điều hướng theo role của user.
 *
 * Luồng chính:
 * 1. ResponseRouter gọi supports() để xác định action thuộc nhóm auth.
 * 2. handle() map action thành cập nhật UI, lưu SessionStore, set currentUser và điều hướng.
 *
 * Business rules:
 * - Login thành công phải lưu User vào session và set userId cho AuctionService/ClientConnection.
 * - Admin mở dashboard riêng; bidder/seller vào lobby và request ROOM_LIST mới nhất.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: phụ thuộc presenter/navigator/session mutable và JavaFX UI.
 * - Dependency: MessageHandler, AuthPresenter, SceneNavigator, SessionStore, RolePolicy, AuctionService.
 */
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
        // Handler này chỉ nhận nhóm action xác thực, các action khác để router chuyển tiếp.
        return switch (action) {
            case "LOGIN_SUCCESS", "LOGIN_FAIL", "REGISTER_SUCCESS", "REGISTER_FAIL", "RESET_SUCCESS", "RESET_FAIL" ->
                    true;
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
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password changed successfully. Please log in again.");
                alert.setHeaderText("Success");
                alert.showAndWait();
                presenter.clearResetForm();
                navigator.showLogin();
            }
            case "RESET_FAIL" -> presenter.showResetError(String.valueOf(message.getData()));
        }
    }

    // Sau khi login thành công, lưu session và điều hướng theo vai trò của user.
    private void handleLoginSuccess(User user) {
        presenter.showLoginSuccess();

        sessionStore.setCurrentUser(user);
        sessionStore.setCurrentUsername(user.getUsername());
        ClientConnection.currentUser = user.getId();
        auctionService.setCurrentUser(user.getId());

        if (rolePolicy.isAdmin(user)) {
            navigator.openAdminDashboard();
        } else {
            // Bidder/Seller vào lobby và yêu cầu danh sách phòng mới nhất.
            navigator.showLobby();
            auctionService.getRooms();
        }
    }
}
