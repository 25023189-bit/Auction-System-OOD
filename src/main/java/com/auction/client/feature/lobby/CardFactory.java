package com.auction.client.feature.lobby;

/**
 * Factory chung để tách logic tạo node UI khỏi presenter.
 */
public interface CardFactory<T, R> {
    R create(T source);
}
