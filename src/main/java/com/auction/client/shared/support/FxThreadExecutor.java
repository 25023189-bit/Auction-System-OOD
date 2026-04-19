package com.auction.client.shared.support;

public interface FxThreadExecutor {
    void execute(Runnable action);
}