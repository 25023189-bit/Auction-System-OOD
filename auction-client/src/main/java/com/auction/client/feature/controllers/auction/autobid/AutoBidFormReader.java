package com.auction.client.feature.controllers.auction.autobid;

import javafx.scene.control.TextField;

public class AutoBidFormReader {
    private final TextField txtMaxBid;
    private final TextField txtAutoBidStep;

    public AutoBidFormReader(TextField txtMaxBid, TextField txtAutoBidStep) {
        this.txtMaxBid = txtMaxBid;
        this.txtAutoBidStep = txtAutoBidStep;
    }

    public AutoBidFormData read() {
        return new AutoBidFormData(clean(txtMaxBid), clean(txtAutoBidStep));
    }

    public void clear() {
        if (txtMaxBid != null) {
            txtMaxBid.clear();
        }
        if (txtAutoBidStep != null) {
            txtAutoBidStep.clear();
        }
    }

    private String clean(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    public record AutoBidFormData(String maxBidText, String stepText) {
    }
}
