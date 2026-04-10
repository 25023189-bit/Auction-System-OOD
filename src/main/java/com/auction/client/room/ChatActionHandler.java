package com.auction.client.room;

import com.auction.server.service.AuctionService;

public class ChatActionHandler implements ActionHandler<ChatRequest> {
    private final AuctionService auctionService;

    public ChatActionHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void handle(ChatRequest request) {
        if (request.content() != null && !request.content().trim().isEmpty()) {
            auctionService.sendChat(request.content().trim());
        }
    }
}