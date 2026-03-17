module com.example.auctionsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens client.controllers to javafx.fxml;
    exports client;
}