package com.auction.client.app.launcher;

import com.auction.client.feature.controllers.AdminController;
import com.auction.client.session.SessionStore;
import com.auction.server.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher mở dashboard quản trị trong một Stage riêng.
 *
 * Vai trò:
 * - Load FXML admin-view và truyền AuctionService hiện tại cho AdminController.
 * - Lưu AdminController vào SessionStore để các response admin cập nhật đúng bảng đang mở.
 *
 * Luồng chính:
 * 1. FXMLLoader nạp admin-view.fxml và lấy controller từ FXML.
 * 2. Controller được gắn service/session, sau đó Stage mới được tạo và hiển thị.
 *
 * Business rules:
 * - Dashboard admin phải dùng cùng AuctionService với phiên đăng nhập hiện tại.
 * - AdminController phải được lưu vào session trước khi server trả dữ liệu admin.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: thao tác Stage/FXML phải chạy trên JavaFX Application Thread.
 * - Dependency: AuctionService, SessionStore, AdminController, FXMLLoader, Stage, SLF4J.
 */
public class AdminDashboardLauncher implements DashboardLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminDashboardLauncher.class);

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

            // Controller admin cần service để gửi lệnh và cần được lưu vào session để nhận phản hồi.
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
            LOGGER.error("Failed to launch admin dashboard.", e);
        }
    }
}
