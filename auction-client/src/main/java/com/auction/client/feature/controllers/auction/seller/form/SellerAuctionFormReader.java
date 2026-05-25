package com.auction.client.feature.controllers.auction.seller.form;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public SellerAuctionFormReadResult read() {
        if (datePickerStart == null || datePickerStart.getValue() == null) {
            return SellerAuctionFormReadResult.error("Please select a start date.");
        }

        try {
            int hour = Integer.parseInt(text(txtStartHour));
            int minute = Integer.parseInt(text(txtStartMinute));
            int extensionSeconds = text(txtExtensionSeconds).isEmpty() ? 60 : Integer.parseInt(text(txtExtensionSeconds));

            return SellerAuctionFormReadResult.ok(new SellerAuctionFormData(
                    text(txtItemName),
                    text(txtItemDescription),
                    Double.parseDouble(text(txtStartingPrice)),
                    Double.parseDouble(text(txtMinimumJoinAmount)),
                    Double.parseDouble(text(txtBidStep)),
                    LocalDateTime.of(datePickerStart.getValue(), LocalTime.of(hour, minute)),
                    Integer.parseInt(text(txtDuration)),
                    extensionSeconds
            ));
        } catch (Exception e) {
            return SellerAuctionFormReadResult.error("Price, minimum join amount, bid step, time, duration, and extension must be valid numbers.");
        }
    }

    public SellerAuctionFormReadResult readJoinAmount() {
        try {
            return SellerAuctionFormReadResult.ok(new SellerAuctionFormData(
                    text(txtItemName),
                    text(txtItemDescription),
                    Double.parseDouble(text(txtStartingPrice)),
                    Double.parseDouble(text(txtMinimumJoinAmount)),
                    1,
                    LocalDateTime.now(),
                    1,
                    60
            ));
        } catch (Exception e) {
            return SellerAuctionFormReadResult.error("");
        }
    }

    public SellerAuctionFormReadResult readDuration() {
        try {
            return SellerAuctionFormReadResult.ok(new SellerAuctionFormData(
                    text(txtItemName),
                    text(txtItemDescription),
                    1,
                    0,
                    1,
                    LocalDateTime.now(),
                    Integer.parseInt(text(txtDuration)),
                    60
            ));
        } catch (Exception e) {
            return SellerAuctionFormReadResult.error("Error: Duration must be a valid integer number");
        }
    }

    public SellerAuctionFormReadResult readExtensionSeconds() {
        try {
            String extensionText = text(txtExtensionSeconds);
            if (extensionText.isEmpty()) {
                return SellerAuctionFormReadResult.error("Error: Extension Seconds cannot be empty!");
            }
            return SellerAuctionFormReadResult.ok(new SellerAuctionFormData(
                    text(txtItemName),
                    text(txtItemDescription),
                    1,
                    0,
                    1,
                    LocalDateTime.now(),
                    1,
                    Integer.parseInt(extensionText)
            ));
        } catch (Exception e) {
            return SellerAuctionFormReadResult.error("Error: Extension must be a valid integer number");
        }
    }

    private String text(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String text(TextArea field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }
}
