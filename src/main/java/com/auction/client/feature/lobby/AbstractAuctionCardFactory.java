package com.auction.client.feature.lobby;

public interface AbstractAuctionCardFactory<T, R> {
    R createDefault(T source);
    R createHighlighted(T source);
}