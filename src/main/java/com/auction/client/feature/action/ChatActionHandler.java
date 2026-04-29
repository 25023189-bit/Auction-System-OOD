package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;

/**
 * Handles chat message actions
 */
public class ChatActionHandler {
    private AuctionService auctionService;

    public ChatActionHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void sendChatMessage(String roomId, String message) {
        if (auctionService != null) {
            auctionService.sendChatMessage(roomId, message);
        }
    }
}
