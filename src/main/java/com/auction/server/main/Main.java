package com.auction.server.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/login-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Sàn Đấu Giá");
        primaryStage.setScene(new Scene(root, 960, 600));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); //Active JavaFX
    }
}