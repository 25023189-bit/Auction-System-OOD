package com.auction.client.launcher;

import com.auction.client.controllers.AdminController;
import com.auction.client.session.SessionStore;
import com.auction.server.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminDashboardLauncher implements DashboardLauncher {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;

    public AdminDashboardLauncher(AuctionService auctionService, SessionStore sessionStore) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
    }

    @Override
    public void launch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/admin-view.fxml"));
            Parent root = loader.load();

            AdminController controller = loader.getController();
            controller.setAuctionService(auctionService);
            sessionStore.setAdminController(controller);

            Stage stage = new Stage();
            stage.setTitle("Hệ Thống Quản Trị - Admin Dashboard");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}