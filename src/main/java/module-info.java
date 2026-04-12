module com.example.auctionprototype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.auction.common.model to javafx.base;

    // 1. MỞ KHÓA CHO JAVAFX TRUY CẬP VÀO THƯ MỤC CONTROLLERS CỦA CLIENT NÀY
    opens com.auction.client.feature.controllers to javafx.fxml;

    opens com.auction.server.main to javafx.graphics, javafx.fxml;
    opens com.example.auctionprototype to javafx.fxml;

    // 2. EXPORT THƯ MỤC NÀY RA (Nếu có dùng ở nơi khác)
    exports com.auction.client.feature.controllers;
    exports com.auction.server.main;
}