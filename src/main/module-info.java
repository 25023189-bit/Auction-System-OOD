module Auction {
    // 1. Khai báo các module cần thiết
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.desktop;
    requires java.sql; // Thêm dòng này để dùng MySQL Connector
    requires jbcrypt;

    // 2. Mở các package chứa controller để JavaFX FXML có thể truy cập (Reflection)
    opens com.auction.client.controllers to javafx.fxml;

    // Nếu bạn có model sử dụng trong FXML (như Bidder, Seller), mở chúng ra
    opens com.auction.common.model to javafx.base;

    // 3. Cho phép JavaFX Graphics truy cập vào package chứa class Main để chạy ứng dụng
    // Giả sử class chạy chính nằm trong com.auction.client hoặc com.auction.server.main
    opens com.auction.client to javafx.graphics;
    opens com.auction.server.main to javafx.graphics;

    // 4. Export các package để các module khác có thể sử dụng (nếu cần)
    exports com.auction.common.dto;
    exports com.auction.common.model;
    exports com.auction.client.controllers;
    exports com.auction.server.service;
}