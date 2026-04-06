module com.example.auctionprototype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    // Cấp quyền cho JavaFX đọc các file Controller
    opens com.auction.client.controllers to javafx.fxml;

    // QUAN TRỌNG: Cấp quyền cho JavaFX khởi tạo class Main
    opens com.auction.server.main to javafx.graphics;

    exports com.auction.client;
    exports com.auction.server.main; // Export nếu cần thiết
}