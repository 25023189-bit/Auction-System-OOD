package com.auction.client.feature.controllers.auction.room.lifecycle;

import com.auction.client.feature.room.AuctionCloseHandler;

public class CloseAuctionController {
    private final AuctionCloseHandler closeHandler;

    public CloseAuctionController(AuctionCloseHandler closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void close(String roomId) {
        closeHandler.closeRoom(roomId);
    }
}
