package com.auction.client.feature.controllers.auction.room.bidding;

import com.auction.client.feature.room.BidActionHandler;
import com.auction.client.feature.room.BidRequest;

public class BidController {
    private final BidActionHandler bidActionHandler;

    public BidController(BidActionHandler bidActionHandler) {
        this.bidActionHandler = bidActionHandler;
    }

    public void submit(String amount) {
        bidActionHandler.handle(new BidRequest(amount));
    }
}
