package com.auction.client.navigation;

import com.auction.client.controllers.AdminController;
import com.auction.client.controllers.SellerController;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.server.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FxSceneNavigator implements SceneNavigator {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/login-view.fxml"));
            loader.setControllerFactory(clazz -> controllerRef);
            Parent root = loader.load();

            Stage stage = resolveStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                windowStateHandler.applyFixed(stage, "Sàn Đấu Giá", 800, 600);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showLobby() {
        try {
            Stage stage = resolveStage();
            windowStateHandler.capture(stage);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/mainLobby-view.fxml"));
            loader.setControllerFactory(clazz -> controllerRef);
            Parent root = loader.load();

            stage = resolveStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, "Sảnh Chính - Hệ Thống Đấu Giá");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showAuctionRoom(AuctionRoom room) {
        try {
            sessionStore.setCurrentRoom(room);
            sessionStore.setCurrentRoomId(room != null ? room.getRoomId() : null);

            Stage stage = resolveStage();
            windowStateHandler.capture(stage);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/auction-view.fxml"));
            loader.setControllerFactory(clazz -> controllerRef);
            Parent root = loader.load();

            stage = resolveStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, "Sàn Đấu Giá - Trong phòng");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/seller-view.fxml"));
            Parent root = loader.load();

            SellerController sellerController = loader.getController();
            sellerController.setAuctionService(auctionService);

            Stage stage = new Stage();
            stage.setTitle("Tạo phiên đấu giá");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/admin-view.fxml"));
            Parent root = loader.load();

            AdminController adminController = loader.getController();
            adminController.setAuctionService(auctionService);
            sessionStore.setAdminController(adminController);

            Stage stage = new Stage();
            stage.setTitle("Hệ Thống Quản Trị - Admin Dashboard");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Stage resolveStage() {
        return (Stage) Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}