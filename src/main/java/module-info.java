module com.example.auctionprototype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.auction.common.model to javafx.base;

    opens com.auction.client.feature.controllers to javafx.fxml;

    opens com.auction.server.main to javafx.graphics, javafx.fxml;
    opens com.example.auctionprototype to javafx.fxml;

    exports com.auction.client.feature.controllers;
    exports com.auction.server.main;
}