package com.auction.client.core.navigation;

import com.auction.client.feature.controllers.AdminController;
import com.auction.client.feature.controllers.SellerController;
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
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, resolveAuctionRoomTitle());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show auction room.", e);
        }
    }

    @Override
    public void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/seller-view.fxml"));
            Parent root = loader.load();

            SellerController sellerController = loader.getController();
            sellerController.setAuctionService(auctionService);

            // Dashboard tạo phiên mở Stage riêng để seller không mất lobby chính.
            Stage stage = new Stage();
            stage.setTitle("Create Auction");
            stage.setScene(new Scene(root));
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.error("Failed to open seller dashboard.", e);
        }
    }

    @Override
    public void openAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/admin-view.fxml"));
            Parent root = loader.load();

            AdminController adminController = loader.getController();
            adminController.setAuctionService(auctionService);
            // Lưu controller admin để AdminFallbackHandler có thể cập nhật bảng khi server phản hồi.
            sessionStore.setAdminController(adminController);

            Stage stage = new Stage();
            stage.setTitle("Admin Dashboard");
            stage.setScene(new Scene(root));
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.error("Failed to open admin dashboard.", e);
        }
    }

    // Tìm Stage đang hiển thị để thay Scene hiện tại.
    private Stage resolveStage() {
        return (Stage) Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    // Factory giúp tái sử dụng controller chính hoặc tạo controller phụ khi cần.
    private Object createController(Class<?> controllerClass) {
        if (controllerClass.isInstance(controllerRef)) {
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
