package com.auction.client.feature.controllers.auction.seller.feedback;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class SellerFormFeedbackController {
    private final Label lblStatus;

    public SellerFormFeedbackController(Label lblStatus) {
        this.lblStatus = lblStatus;
    }

    public void showError(String message) {
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
}
