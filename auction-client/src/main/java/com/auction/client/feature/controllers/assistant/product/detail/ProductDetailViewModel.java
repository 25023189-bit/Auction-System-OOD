package com.auction.client.feature.controllers.assistant.product.detail;

public record ProductDetailViewModel(String title, String description, String startingPrice, String base64Image) {
    public ProductDetailViewModel(String title, String description, String startingPrice) {
        this(title, description, startingPrice, "");
    }
}
