package com.auction.client.feature.controllers.auction.seller.validation;

public record SellerAuctionValidationResult(boolean valid, String message) {
    public static SellerAuctionValidationResult ok() {
        return new SellerAuctionValidationResult(true, "");
    }

    public static SellerAuctionValidationResult error(String message) {
        return new SellerAuctionValidationResult(false, message);
    }
}
