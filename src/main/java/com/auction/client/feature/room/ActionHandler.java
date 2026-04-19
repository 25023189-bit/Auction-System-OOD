package com.auction.client.feature.room;

public interface ActionHandler<T> {
    void handle(T request);
}