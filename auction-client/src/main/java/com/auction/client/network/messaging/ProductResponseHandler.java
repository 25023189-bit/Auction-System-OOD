package com.auction.client.network.messaging;

import com.auction.client.service.AuctionService;
import com.auction.common.dto.Message;

public class ProductResponseHandler implements MessageHandler {
    private final AuctionService auctionService;

    public ProductResponseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public boolean supports(String action) {
        return "PRODUCT_DETAILS_SUCCESS".equals(action)
                || "PRODUCT_DETAILS_ERROR".equals(action)
                || "PRODUCT_DETAILS_FAIL".equals(action);
    }

    @Override
    public void handle(Message message) {
        if (auctionService != null) {
            auctionService.fireProductDetailsReceived(message.getData());
        }
    }
}
