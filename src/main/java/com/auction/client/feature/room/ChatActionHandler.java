package com.auction.client.feature.room;

import com.auction.server.service.AuctionService;

/**
 * Xử lý gửi chat trong phòng đấu giá.
 */
public class ChatActionHandler implements ActionHandler<ChatRequest> {
    private final AuctionService auctionService;

    public ChatActionHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void handle(ChatRequest request) {
        // Không gửi tin nhắn rỗng lên server.
        if (request.content() != null && !request.content().trim().isEmpty()) {
            auctionService.sendChat(request.content().trim());
        }
    }
}
