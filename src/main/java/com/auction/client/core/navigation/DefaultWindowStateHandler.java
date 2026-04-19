package com.auction.client.core.navigation;

import javafx.stage.Stage;

public class DefaultWindowStateHandler implements WindowStateHandler {
    private boolean lastMaximized = false;
    private double lastWidth = 1280;
    private double lastHeight = 720;

    @Override
    public void capture(Stage stage) {
        if (stage == null) return;

        lastMaximized = stage.isMaximized();
        if (!lastMaximized) {
            lastWidth = stage.getWidth();
            lastHeight = stage.getHeight();
        }
    }

    @Override
    public void apply(Stage stage, String title) {
        if (stage == null) return;

        stage.setTitle(title);
        stage.setMaximized(lastMaximized);

        if (!lastMaximized) {
            stage.setWidth(lastWidth > 0 ? lastWidth : 1280);
            stage.setHeight(lastHeight > 0 ? lastHeight : 720);
            stage.centerOnScreen();
        }
    }

    @Override
    public void applyFixed(Stage stage, String title, double width, double height) {
        if (stage == null) return;

        stage.setTitle(title);
        stage.setMaximized(false);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();

        lastMaximized = false;
        lastWidth = width;
        lastHeight = height;
    }
}