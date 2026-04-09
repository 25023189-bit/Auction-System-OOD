package com.auction.client.lobby;

public interface CardFactory<T, R> {
    R create(T source);
}