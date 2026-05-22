package com.auction.client.feature.controllers.auction.seller.feedback;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;

public class SellerFormFeedbackController {
    private final Label lblStatus;

    public SellerFormFeedbackController(Label lblStatus) {
        this.lblStatus = lblStatus;
    }

    public void showError(String message) {
        error(message);
    }

    public void error(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            lblStatus.setTextFill(Color.RED);
        }
    }

    public void clear() {
        if (lblStatus != null) {
            lblStatus.setText("");
        }
    }

    public void markInvalid(TextInputControl field) {
        if (field != null) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    public void markNormal(TextInputControl field) {
        if (field != null) {
            field.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1px;");
        }
    }
}
