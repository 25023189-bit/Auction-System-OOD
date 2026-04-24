package com.auction.client.feature.room;

import com.auction.server.service.AuctionService;

public class AuctionCloseHandler {
    private final AuctionService auctionService;

    public AuctionCloseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void closeRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        auctionService.closeAuction(roomId);
    }
}
