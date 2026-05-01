package com.auction.client.shared.support;

/**
 * Hợp đồng chạy tác vụ cập nhật UI đúng JavaFX thread.
 *
 * Vai trò:
 * - Chuẩn hóa cách đưa response từ thread nền về UI thread.
 * - Giúp controller không phụ thuộc trực tiếp vào Platform.runLater.
 *
 * Luồng chính:
 * 1. Caller truyền Runnable cập nhật UI vào execute().
 * 2. Implementation quyết định chạy ngay hoặc enqueue lên JavaFX thread.
 *
 * Business rules:
 * - Action cập nhật control JavaFX phải chạy trên JavaFX Application Thread.
 * - Implementation không được bỏ qua action hợp lệ.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; DefaultFxThreadExecutor stateless.
 * - Dependency: Runnable và implementation DefaultFxThreadExecutor.
 */
public interface FxThreadExecutor {
    void execute(Runnable action);
}
