module Auction {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires jbcrypt;

    opens client.controllers to javafx.fxml;

    opens client to javafx.graphics;

    exports server.main;
}