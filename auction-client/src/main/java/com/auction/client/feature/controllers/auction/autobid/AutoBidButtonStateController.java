package com.auction.client.feature.controllers.auction.autobid;

import javafx.scene.control.Button;

public class AutoBidButtonStateController {
    private final Button btnToggleAutoBid;

    public AutoBidButtonStateController(Button btnToggleAutoBid) {
        this.btnToggleAutoBid = btnToggleAutoBid;
    }

    public void markOn() {
        if (btnToggleAutoBid != null) {
            btnToggleAutoBid.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnToggleAutoBid.setText("AUTO BID: ON");
            btnToggleAutoBid.setDisable(false);
        }
    }

    public void markOff() {
        if (btnToggleAutoBid != null) {
            btnToggleAutoBid.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnToggleAutoBid.setText("AUTO BID ⚙");
            btnToggleAutoBid.setDisable(false);
        }
    }

    public void markPending() {
        if (btnToggleAutoBid != null) {
            btnToggleAutoBid.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnToggleAutoBid.setText("AUTO BID...");
            btnToggleAutoBid.setDisable(true);
        }
    }
}
