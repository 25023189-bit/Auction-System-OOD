package com.auction.client.feature.controllers.auction.lobby;

import com.auction.client.service.AuctionService;

public class LobbyController {
    private final AuctionService auctionService;

    public LobbyController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void refreshRooms() {
        auctionService.getRooms();
    }
}
