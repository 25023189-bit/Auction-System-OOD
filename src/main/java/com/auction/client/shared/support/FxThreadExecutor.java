package com.auction.client.shared.support;

/**
 * Hợp đồng chạy tác vụ cập nhật UI đúng JavaFX thread.
 */
public interface FxThreadExecutor {
    void execute(Runnable action);
}
