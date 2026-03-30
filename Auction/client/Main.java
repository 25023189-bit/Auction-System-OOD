package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/auction-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Sàn Đấu Giá VIP PRO - Client");
        primaryStage.setScene(new Scene(root, 450, 650));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); //Active JavaFX
    }
}
