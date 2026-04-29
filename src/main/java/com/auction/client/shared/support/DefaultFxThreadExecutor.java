package com.auction.client.shared.support;

import javafx.application.Platform;

/**
 * Bảo đảm mọi cập nhật UI chạy trên JavaFX Application Thread.
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
