package com.auction.client.app.launcher;

import com.auction.client.feature.controllers.SellerController;
import com.auction.server.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mở form tạo phiên đấu giá cho seller ở một cửa sổ riêng.
 */
public class SellerDashboardLauncher implements DashboardLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerDashboardLauncher.class);

    private final AuctionService auctionService;

    public SellerDashboardLauncher(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void launch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/seller-view.fxml"));
            Parent root = loader.load();

            // Truyền AuctionService để form có thể gửi request tạo phiên lên server.
            SellerController controller = loader.getController();
            controller.setAuctionService(auctionService);

            Stage stage = new Stage();
            stage.setTitle("Create Auction");
            stage.setScene(new Scene(root));
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.error("Failed to launch seller dashboard.", e);
        }
    }
}
