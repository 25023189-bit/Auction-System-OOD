package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/views/login-view.fxml"));

        if (fxmlLoader.getLocation() == null) {
            System.out.println("CẢNH BÁO: Không tìm thấy file login-view.fxml");
            return;
        }

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hệ thống Đấu Giá - Đăng Nhập");
        stage.setScene(scene);
        stage.show();
    }
}