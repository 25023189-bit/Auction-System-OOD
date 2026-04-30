package com.auction.server.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Entry point JavaFX của ứng dụng client khi chạy từ module server.main.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/login-view.fxml"));
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
