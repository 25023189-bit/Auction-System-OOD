package com.auction.client.feature.controllers.auction.notify;

public enum EndAuctionResultType {
    BIDDER_WIN,
    BIDDER_LOSE,
    SELLER_SOLD,
    SELLER_NO_WINNER,
    CLOSED_BY_SELLER,
    CLOSED_BY_ADMIN,
    NO_WINNER,
    UNKNOWN
}
