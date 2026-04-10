package com.auction.client.support;

import javafx.stage.Stage;
import javafx.stage.Window;

public class DefaultStageLocator implements StageLocator {
    @Override
    public Stage resolveMainStage() {
        return (Stage) Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}