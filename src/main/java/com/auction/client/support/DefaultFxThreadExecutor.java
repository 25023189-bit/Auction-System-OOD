package com.auction.client.support;

import javafx.application.Platform;

public class DefaultFxThreadExecutor implements FxThreadExecutor {
    @Override
    public void execute(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}