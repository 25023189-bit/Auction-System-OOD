package com.auction.client.core.navigation;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DefaultWindowStateHandler implements WindowStateHandler {

    @Override
    public void capture(Stage stage) {
        // Scene transitions should not preserve smaller previous window sizes.
    }

    @Override
    public void apply(Stage stage, String title) {
        if (stage == null) return;

        stage.setTitle(title);
        stage.setFullScreen(false);
        maximize(stage);
    }

    @Override
    public void applyFixed(Stage stage, String title, double width, double height) {
        apply(stage, title);
    }

    private void maximize(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setMaximized(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(true);

        Platform.runLater(() -> {
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);
        });
    }
}
