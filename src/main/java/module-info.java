module com.auction {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    exports com.auction.client.app.launcher;
    exports com.auction.client.feature.auth;
    exports com.auction.client.feature.lobby;
    exports com.auction.client.feature.room;
    exports com.auction.server.main;
    exports com.auction.common.dto;
    exports com.auction.common.model;

    opens com.auction.client.app.launcher to javafx.fxml;
    opens com.auction.client.feature.auth to javafx.fxml;
    opens com.auction.client.feature.lobby to javafx.fxml;
    opens com.auction.client.feature.room to javafx.fxml;
    opens com.auction.client.feature.controllers to javafx.fxml;
    opens com.auction.server.main to javafx.fxml;
}