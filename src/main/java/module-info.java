module com.example.auctionprototype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;
    requires org.slf4j;

    requires java.mail;
    opens com.auction.common.model to javafx.base;

    opens com.auction.client.feature.controllers to javafx.fxml;
    opens com.auction.client.AI.chatbot to javafx.fxml;

    opens com.auction.client.app to javafx.graphics, javafx.fxml;
    //opens com.example.auctionprototype to javafx.fxml;

    exports com.auction.client.feature.controllers;
    exports com.auction.client.AI.chatbot;
    exports com.auction.common.dto;
    exports com.auction.common.model;
    exports com.auction.server;
    exports com.auction.server.main;
    exports com.auction.client.service;
    exports com.auction.client.network.socket;
    opens com.auction.server.dao to org.mockito;
    opens com.auction.server.service to org.mockito;
}
