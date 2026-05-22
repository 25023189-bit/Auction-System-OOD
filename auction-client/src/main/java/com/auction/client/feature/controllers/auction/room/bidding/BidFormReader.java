package com.auction.client.feature.controllers.auction.room.bidding;

import javafx.scene.control.TextField;

public class BidFormReader {
    private final TextField txtBidAmount;

    public BidFormReader(TextField txtBidAmount) {
        this.txtBidAmount = txtBidAmount;
    }

    public String readAmount() {
        return txtBidAmount == null || txtBidAmount.getText() == null ? "" : txtBidAmount.getText();
    }
}
