package com.auction.client.feature.product; // Sửa dòng này

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.auction.common.model.AuctionRoom;

public class ProductViewController {
    @FXML private Label lblProductName;
    @FXML private Label lblDescription;
    @FXML private Label lblStartingPrice;

    public void setProductData(AuctionRoom room) {
        lblProductName.setText(room.getItemName());
        lblDescription.setText(room.getItemDescription());
        lblStartingPrice.setText(String.valueOf(room.getStartingPrice()));
    }
}