package com.auction.client.core.navigation;

import javafx.stage.Stage;

public interface WindowStateHandler {
    void capture(Stage stage);

    void apply(Stage stage, String title);

    void applyFixed(Stage stage, String title, double width, double height);
}