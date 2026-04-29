package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;

/**
 * Handler gửi tin nhắn chat phiên bản dùng cho package action cũ.
 */
public class ChatActionHandler {
    private AuctionService auctionService;

    public ChatActionHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void sendChatMessage(String roomId, String message) {
        if (auctionService != null) {
            // Gửi cả roomId để server biết tin nhắn thuộc phòng nào.
            auctionService.sendChatMessage(roomId, message);
        }
    }
}
