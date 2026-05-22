package com.auction.client.feature.controllers.account.admin.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AdminDialogController {
    public void show(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public boolean confirm(String title, String headerText) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(headerText);
        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
