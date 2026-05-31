package com.auction.client.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Entry point JavaFX mở giao diện client từ module hiện tại.
 *
 * Vai trò:
 * - Load màn hình login ban đầu của ứng dụng JavaFX.
 * - Cấu hình kích thước cửa sổ chính để các view kế tiếp dùng cùng stage.
 *
 * Luồng chính:
 * 1. JavaFX gọi start(), FXML login-view được load và gắn vào Scene.
 * 2. Stage được đặt title, min size, kích thước theo màn hình và hiển thị.
 *
 * Business rules:
 * - Ứng dụng luôn bắt đầu ở màn hình login.
 * - Cửa sổ chính giữ kích thước tối thiểu 1000x600 để layout các view đủ không gian.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: chạy trên JavaFX Application Thread.
 * - Dependency: JavaFX Application, FXMLLoader, Stage, Screen và resource login-view.fxml.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Thêm /com/example/auctionprototype/fxml/ vào trước tên file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/login-view.fxml"));
        Parent root = loader.load();

        // Mở màn hình login ở kích thước tối đa để các view phía sau kế thừa cùng trạng thái cửa sổ.
        primaryStage.setTitle("Auction System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.setFullScreen(false);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
        primaryStage.show();
        Platform.runLater(() -> primaryStage.setMaximized(true));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
