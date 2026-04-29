package com.auction.client.shared.support;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Tìm Stage chính đang hiển thị của ứng dụng.
 */
public class DefaultStageLocator implements StageLocator {
    @Override
    public Stage resolveMainStage() {
        return (Stage) Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}
