package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;

/**
 * Handles auction closing actions
 */
public class AuctionCloseHandler {
    private AuctionService auctionService;

    public AuctionCloseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void closeRoom(String roomId) {
        if (auctionService != null) {
            auctionService.closeAuction(roomId);
        }
    }
}
