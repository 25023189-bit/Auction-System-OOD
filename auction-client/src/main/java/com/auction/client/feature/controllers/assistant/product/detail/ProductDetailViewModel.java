package com.auction.client.feature.controllers.assistant.product.detail;

public record ProductDetailViewModel(
        String title,
        String description,
        String currentPrice,
        String bidCount,
        String timeRemaining,
        String base64Image
) {
}
