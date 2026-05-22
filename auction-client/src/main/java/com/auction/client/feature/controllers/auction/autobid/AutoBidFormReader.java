package com.auction.client.feature.controllers.auction.autobid;

import javafx.scene.control.TextField;

public class AutoBidFormReader {
    private final TextField txtMaxBid;
    private final TextField txtAutoBidStep;

    public AutoBidFormReader(TextField txtMaxBid, TextField txtAutoBidStep) {
        this.txtMaxBid = txtMaxBid;
        this.txtAutoBidStep = txtAutoBidStep;
    }

    public double readMaxBid() {
        return Double.parseDouble(clean(txtMaxBid));
    }

    public double readStep() {
        return Double.parseDouble(clean(txtAutoBidStep));
    }

    private String clean(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().replaceAll("[^\\d.]", "");
    }
}
