package com.auction.client.shared.support;

import javafx.application.Platform;

/**
 * Executor bảo đảm tác vụ UI chạy trên JavaFX Application Thread.
 *
 * Vai trò:
 * - Chạy ngay action nếu đang ở JavaFX thread.
 * - Đưa action vào Platform.runLater nếu được gọi từ thread nền như reader socket.
 *
 * Luồng chính:
 * 1. Controller hoặc network handler gọi execute(action) trước khi cập nhật UI.
 * 2. Executor kiểm tra Platform.isFxApplicationThread() và chọn cách chạy phù hợp.
 *
 * Business rules:
 * - Mọi cập nhật control JavaFX từ response server phải đi qua JavaFX thread.
 * - Action truyền vào không được null theo kỳ vọng của caller.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, delegate sang Platform.
 * - Dependency: FxThreadExecutor, Platform.
 */
public class DefaultFxThreadExecutor implements FxThreadExecutor {
    @Override
    public void execute(Runnable action) {
        // Nếu đã ở JavaFX thread thì chạy ngay, nếu không thì đưa vào Platform.runLater.
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
