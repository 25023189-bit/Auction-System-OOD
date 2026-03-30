module Auction {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.base;

    opens client.controllers to javafx.fxml;

    opens client to javafx.graphics;

    exports common.DTO;
}