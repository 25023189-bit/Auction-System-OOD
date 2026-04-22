package com.auction.client.core.navigation;

import com.auction.client.feature.controllers.AdminController;
import com.auction.client.feature.controllers.SellerController;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
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
                windowStateHandler.apply(stage, "Auction System");
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
                windowStateHandler.apply(stage, "Main Lobby - Auction System");
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource(resolveAuctionRoomViewPath()));
            loader.setControllerFactory(clazz -> controllerRef);
            Parent root = loader.load();

            stage = resolveStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                windowStateHandler.apply(stage, resolveAuctionRoomTitle());
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
            stage.setTitle("Create Auction");
            stage.setScene(new Scene(root));
            stage.setFullScreen(false);
            stage.setMaximized(true);
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
            stage.setTitle("Admin Dashboard");
            stage.setScene(new Scene(root));
            stage.setFullScreen(false);
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

    private String resolveAuctionRoomViewPath() {
        User currentUser = sessionStore != null ? sessionStore.getCurrentUser() : null;
        String role = currentUser != null ? currentUser.getRole() : null;

        if ("SELLER".equalsIgnoreCase(role)) {
            return "/com/example/auctionprototype/seller-auction-view.fxml";
        }
        return "/com/example/auctionprototype/bidder-auction-view.fxml";
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
