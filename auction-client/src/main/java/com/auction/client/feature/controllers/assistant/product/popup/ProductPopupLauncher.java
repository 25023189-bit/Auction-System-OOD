package com.auction.client.feature.controllers.assistant.product.popup;

import com.auction.client.feature.controllers.assistant.product.ProductViewController;
import com.auction.client.service.AuctionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductPopupLauncher {
    private final AuctionService auctionService;

    public ProductPopupLauncher(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void open(String roomId) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/product-view.fxml"));
        Parent root = loader.load();
        ProductViewController controller = loader.getController();
        controller.setAuctionService(auctionService);
        controller.setRoomId(roomId);

        Stage stage = new Stage();
        stage.setTitle("Chi tiết sản phẩm");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
