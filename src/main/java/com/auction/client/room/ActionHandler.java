package com.auction.client.room;

public interface ActionHandler<T> {
    void handle(T request);
}