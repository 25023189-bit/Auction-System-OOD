package com.auction.client.feature.controllers.shared.alert;

import javafx.scene.control.Alert;

public class AlertController {
    public void show(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
