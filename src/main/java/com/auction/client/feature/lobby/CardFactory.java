package com.auction.client.feature.lobby;

public interface CardFactory<T, R> {
    R create(T source);
}