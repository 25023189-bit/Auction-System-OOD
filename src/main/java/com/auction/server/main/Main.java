package com.auction.server.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;


import com.auction.server.utils.DatabaseConnection;
import com.auction.server.utils.PasswordUtil;
// SỬA: Import đúng địa chỉ model AuctionRoom
import com.auction.common.model.AuctionRoom;

import java.sql.Connection;

/**
 * Lớp Main dùng để chạy thử nghiệm (Test) các thành phần độc lập của Server
 * như Kết nối Database và mã hóa mật khẩu trước khi chạy Server chính thức.
 */

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auction-view.fxml"));
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