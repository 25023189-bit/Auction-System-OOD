package com.auction.client.feature.controllers.shared.thread;

import javafx.application.Platform;

public class FxThreadController {
    public void execute(Runnable action) {
        if (action == null) return;
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
