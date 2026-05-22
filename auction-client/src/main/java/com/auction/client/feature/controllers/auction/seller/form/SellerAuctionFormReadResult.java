package com.auction.client.feature.controllers.auction.seller.form;

public record SellerAuctionFormReadResult(boolean valid, SellerAuctionFormData data, String message) {
    public static SellerAuctionFormReadResult ok(SellerAuctionFormData data) {
        return new SellerAuctionFormReadResult(true, data, "");
    }

    public static SellerAuctionFormReadResult error(String message) {
        return new SellerAuctionFormReadResult(false, null, message);
    }
}
