package com.auction.server.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/auction-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Sàn Đấu Giá VIP PRO - Client");
        primaryStage.setScene(new Scene(root, 500, 400));
        // Thêm 2 dòng này để chặn thu nhỏ cửa sổ quá mức
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); //Active JavaFX
    }
}