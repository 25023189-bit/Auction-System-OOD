package com.auction.client.feature.controllers.assistant.product;

import com.auction.client.service.AuctionService;

import java.util.function.Consumer;

public class ProductDetailsResponseController {
    private final AuctionService auctionService;

    public ProductDetailsResponseController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void listen(Consumer<Object> callback) {
        auctionService.setProductDetailsCallback(callback);
    }
}
