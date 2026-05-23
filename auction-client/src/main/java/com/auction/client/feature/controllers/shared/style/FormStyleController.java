package com.auction.client.feature.controllers.shared.style;

import javafx.scene.control.Control;

public class FormStyleController {
    public void markInvalid(Control control) {
        if (control != null) {
            control.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    public void markNeutral(Control control) {
        if (control != null) {
            control.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1px;");
        }
    }
}
