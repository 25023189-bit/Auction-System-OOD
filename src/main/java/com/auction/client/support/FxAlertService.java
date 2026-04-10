package com.auction.client.support;

import javafx.scene.control.Alert;

public class FxAlertService implements AlertService {
    @Override
    public void show(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void info(String title, String content) {
        show(Alert.AlertType.INFORMATION, title, null, content);
    }

    @Override
    public void warning(String title, String content) {
        show(Alert.AlertType.WARNING, title, null, content);
    }

    @Override
    public void error(String title, String content) {
        show(Alert.AlertType.ERROR, title, null, content);
    }
}