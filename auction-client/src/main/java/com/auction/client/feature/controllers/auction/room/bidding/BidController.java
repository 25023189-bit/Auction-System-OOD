package com.auction.client.feature.controllers.auction.room.bidding;

import com.auction.client.feature.room.BidActionHandler;
import com.auction.client.feature.room.BidRequest;
import javafx.scene.control.TextField;

public class BidController {
    private final BidActionHandler bidActionHandler;
    private final BidFormReader formReader;

    public BidController(BidActionHandler bidActionHandler, TextField txtBidAmount) {
        this.bidActionHandler = bidActionHandler;
        this.formReader = new BidFormReader(txtBidAmount);
    }

    public void handleBid() {
        if (bidActionHandler != null) {
            bidActionHandler.handle(new BidRequest(formReader.readAmount()));
        }
        formReader.clear();
    }
}
