package com.auction.client.lobby;

public interface AbstractAuctionCardFactory<T, R> {
    R createDefault(T source);
    R createHighlighted(T source);
}