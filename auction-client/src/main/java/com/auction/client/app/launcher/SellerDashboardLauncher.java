package com.auction.client.app.launcher;

import com.auction.client.feature.controllers.SellerController;
import com.auction.client.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher mở form tạo phiên đấu giá cho seller trong Stage riêng.
 *
 * Vai trò:
 * - Load seller-view.fxml và lấy SellerController do FXMLLoader tạo.
 * - Truyền AuctionService hiện tại để form gửi yêu cầu tạo phiên lên server.
 *
 * Luồng chính:
 * 1. FXMLLoader nạp seller-view.fxml và lấy controller.
 * 2. Controller được gắn service, Stage mới được tạo, maximize và hiển thị.
 *
 * Business rules:
 * - Form tạo phiên phải dùng phiên đăng nhập/socket hiện tại của seller.
 * - Mở form ở cửa sổ phụ để seller không mất lobby chính.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: thao tác Stage/FXML phải chạy trên JavaFX Application Thread.
 * - Dependency: AuctionService, SellerController, FXMLLoader, Stage, SLF4J.
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/seller-view.fxml"));
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
