package com.auction.client.feature.controllers.auction.seller.form;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class SellerAuctionFormReader {
    private final TextField txtItemName;
    private final TextArea txtItemDescription;
    private final TextField txtStartingPrice;
    private final TextField txtMinimumJoinAmount;
    private final TextField txtBidStep;
    private final DatePicker datePickerStart;
    private final TextField txtStartHour;
    private final TextField txtStartMinute;
    private final TextField txtDuration;
    private final TextField txtExtensionSeconds;

    public SellerAuctionFormReader(
            TextField txtItemName,
            TextArea txtItemDescription,
            TextField txtStartingPrice,
            TextField txtMinimumJoinAmount,
            TextField txtBidStep,
            DatePicker datePickerStart,
            TextField txtStartHour,
            TextField txtStartMinute,
            TextField txtDuration,
            TextField txtExtensionSeconds
    ) {
        this.txtItemName = txtItemName;
        this.txtItemDescription = txtItemDescription;
        this.txtStartingPrice = txtStartingPrice;
        this.txtMinimumJoinAmount = txtMinimumJoinAmount;
        this.txtBidStep = txtBidStep;
        this.datePickerStart = datePickerStart;
        this.txtStartHour = txtStartHour;
        this.txtStartMinute = txtStartMinute;
        this.txtDuration = txtDuration;
        this.txtExtensionSeconds = txtExtensionSeconds;
    }

    public SellerAuctionFormData read() {
        return new SellerAuctionFormData(
                text(txtItemName),
                text(txtItemDescription),
                text(txtStartingPrice),
                text(txtMinimumJoinAmount),
                text(txtBidStep),
                datePickerStart != null ? datePickerStart.getValue() : null,
                text(txtStartHour),
                text(txtStartMinute),
                text(txtDuration),
                text(txtExtensionSeconds)
        );
    }

    private String text(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String text(TextArea field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }
}
