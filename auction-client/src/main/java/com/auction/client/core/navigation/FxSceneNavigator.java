package com.auction.client.core.navigation;

import com.auction.client.feature.controllers.app.root.AuctionController;
import com.auction.client.feature.controllers.app.navigation.DashboardLauncherController;
import com.auction.client.feature.controllers.auction.notify.EndAuctionNotificationViewModel;
import com.auction.client.feature.controllers.auction.notify.NotifyEndAuctionController;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.client.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SceneNavigator triển khai điều hướng màn hình bằng JavaFX FXMLLoader.
 *
 * Vai trò:
 * - Chuyển giữa login, lobby, auction room và mở các dashboard phụ.
 * - Chọn FXML theo role hiện tại và giữ lại service/session khi thay scene.
 *
 * Luồng chính:
 * 1. Controller gọi showLogin(), showLobby(), showAuctionRoom() hoặc open dashboard theo hành động người dùng.
 * 2. Navigator load FXML, tạo/tái sử dụng controller, gắn scene vào Stage và áp dụng WindowStateHandler.
 *
 * Business rules:
 * - Seller và bidder dùng FXML lobby/room khác nhau theo quyền thao tác.
 * - Trước khi vào auction room phải lưu room hiện tại vào SessionStore để controller mới bind đúng dữ liệu.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: FXMLLoader, Stage và Window phải thao tác trên JavaFX Application Thread.
 * - Dependency: SceneNavigator, WindowStateHandler, SessionStore, AuctionService, FXMLLoader, Stage, User/AuctionRoom.
 */
public class FxSceneNavigator implements SceneNavigator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FxSceneNavigator.class);

    // Controller hiện tại được tái sử dụng cho các FXML chung để không mất kết nối socket.
    private final Object controllerRef;
    private final WindowStateHandler windowStateHandler;
    private final SessionStore sessionStore;
    private final AuctionService auctionService;
    private Stage mainStage;

    public FxSceneNavigator(Object controllerRef,
                            WindowStateHandler windowStateHandler,
                            SessionStore sessionStore,
                            AuctionService auctionService) {
        this.controllerRef = controllerRef;
        this.windowStateHandler = windowStateHandler;
        this.sessionStore = sessionStore;
        this.auctionService = auctionService;
    }

    @Override
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/login-view.fxml"));
            // Cho phép FXMLLoader dùng lại controllerRef nếu FXML trỏ cùng controller.
            loader.setControllerFactory(this::createController);
            Parent root = loader.load();

            Stage stage = resolveStage();
            if (stage != null) {
                rememberMainStage(stage);
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, "Auction System");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show login screen.", e);
        }
    }

    @Override
    public void showLobby() {
        try {
            Stage stage = resolveStage();
            // Lưu trạng thái cửa sổ trước khi thay Scene.
            windowStateHandler.capture(stage);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(resolveLobbyViewPath()));
            loader.setControllerFactory(this::createController);
            Parent root = loader.load();

            stage = resolveStage();
            if (stage != null) {
                rememberMainStage(stage);
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, resolveLobbyTitle());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show lobby screen.", e);
        }
    }

    @Override
    public void showAuctionRoom(AuctionRoom room) {
        try {
            // Lưu room hiện tại trước khi nạp FXML để controller mới bind đúng dữ liệu.
            sessionStore.setCurrentRoom(room);
            sessionStore.setCurrentRoomId(room != null ? room.getRoomId() : null);

            Stage stage = resolveStage();
            windowStateHandler.capture(stage);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(resolveAuctionRoomViewPath()));
            loader.setControllerFactory(this::createController);
            Parent root = loader.load();

            stage = resolveStage();
            if (stage != null) {
                rememberMainStage(stage);
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, resolveAuctionRoomTitle());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show auction room.", e);
        }
    }

    @Override
    public void openSellerDashboard() {
        new DashboardLauncherController(auctionService, sessionStore).openSellerDashboard();
    }

    @Override
    public void showEndAuctionNotification(EndAuctionNotificationViewModel notificationData) {
        try {
            Stage stage = resolveStage();
            windowStateHandler.capture(stage);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/notify-end-auction-view.fxml"));
            Parent root = loader.load();

            NotifyEndAuctionController controller = loader.getController();
            controller.setNotificationData(notificationData);
            controller.setActionHandlers(
                    this::showLobby,
                    this::openSellerDashboard,
                    this::showLobby
            );

            stage = resolveStage();
            if (stage != null) {
                rememberMainStage(stage);
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, "Auction Ended - Auction System");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show auction end notification.", e);
        }
    }

    @Override
    public void openAdminDashboard() {
        new DashboardLauncherController(auctionService, sessionStore).openAdminDashboard();
    }

    // Tìm Stage đang hiển thị để thay Scene hiện tại.
    private Stage resolveStage() {
        if (mainStage != null && mainStage.isShowing()) {
            return mainStage;
        }

        Stage stage = Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast)
                .filter(this::isMainApplicationStage)
                .findFirst()
                .orElse(null);
        rememberMainStage(stage);
        return stage;
    }

    private void rememberMainStage(Stage stage) {
        if (stage != null && isMainApplicationStage(stage)) {
            mainStage = stage;
        }
    }

    private boolean isMainApplicationStage(Stage stage) {
        if (stage == null) return false;
        String title = stage.getTitle();
        return title == null
                || title.isBlank()
                || "Auction System".equals(title)
                || "Auction Ended - Auction System".equals(title)
                || title.endsWith("Lobby - Auction System")
                || title.startsWith("Auction Room");
    }

    // Factory giúp tái sử dụng controller chính hoặc tạo controller phụ khi cần.
    private Object createController(Class<?> controllerClass) {
        if (controllerClass.isInstance(controllerRef)) {
            if (controllerRef instanceof AuctionController auctionController) {
                auctionController.prepareForFxmlReload();
            }
            return controllerRef;
        }

        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create controller: " + controllerClass.getName(), e);
        }
    }

    // Seller và bidder dùng FXML phòng khác nhau vì quyền thao tác khác nhau.
    private String resolveAuctionRoomViewPath() {
        User currentUser = sessionStore != null ? sessionStore.getCurrentUser() : null;
        String role = currentUser != null ? currentUser.getRole() : null;

        if ("SELLER".equalsIgnoreCase(role)) {
            return "/com/example/auctionprototype/fxml/seller-auction-view.fxml";
        }
        return "/com/example/auctionprototype/fxml/bidder-auction-view.fxml";
    }

    // Chọn lobby theo role để seller có nút tạo phiên, bidder chỉ tham gia phòng.
    private String resolveLobbyViewPath() {
        User currentUser = sessionStore != null ? sessionStore.getCurrentUser() : null;
        String role = currentUser != null ? currentUser.getRole() : null;

        if ("SELLER".equalsIgnoreCase(role)) {
            return "/com/example/auctionprototype/fxml/seller-mainLobby-view.fxml";
        }
        return "/com/example/auctionprototype/fxml/bidder-mainLobby-view.fxml";
    }

    private String resolveLobbyTitle() {
        User currentUser = sessionStore != null ? sessionStore.getCurrentUser() : null;
        String role = currentUser != null ? currentUser.getRole() : null;

        if ("SELLER".equalsIgnoreCase(role)) {
            return "Seller Lobby - Auction System";
        }
        return "Bidder Lobby - Auction System";
    }

    private String resolveAuctionRoomTitle() {
        User currentUser = sessionStore != null ? sessionStore.getCurrentUser() : null;
        String role = currentUser != null ? currentUser.getRole() : null;

        if ("SELLER".equalsIgnoreCase(role)) {
            return "Auction Room - Seller View";
        }
        return "Auction Room - Bidder View";
    }
}
