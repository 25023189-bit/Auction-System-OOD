module com.example.auctionprototype {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.auctionprototype to javafx.fxml;
    exports com.example.auctionprototype;
}