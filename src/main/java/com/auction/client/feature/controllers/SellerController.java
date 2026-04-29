package com.auction.client.feature.controllers;

import com.auction.server.service.AuctionService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SellerController {

    @FXML
    private TextField txtItemName;
    @FXML
    private TextArea txtItemDescription;
    @FXML
    private TextField txtStartingPrice;
    @FXML
    private TextField txtMinimumJoinAmount;
    @FXML
    private TextField txtBidStep;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblReputation;
    @FXML
    private DatePicker datePickerStart;
    @FXML
    private TextField txtStartHour;
    @FXML
    private TextField txtStartMinute;
    @FXML
    private TextField txtDuration;
    @FXML
    private TextField txtExtensionSeconds;

    private AuctionService auctionService;

    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    @FXML
    public void handleCreateAuction() {
        if (auctionService == null) {
            showError("Error: Service is not available.");
            return;
        }

        LocalDate date = datePickerStart.getValue();
        if (date == null) {
            showError("Please select a start date.");
            return;
        }

        try {
            String itemName = safeText(txtItemName);
            String itemDesc = safeText(txtItemDescription);
            double startingPrice = Double.parseDouble(safeText(txtStartingPrice));
            double minimumJoinAmount = Double.parseDouble(safeText(txtMinimumJoinAmount));
            double bidStep = Double.parseDouble(safeText(txtBidStep));
            int hour = Integer.parseInt(safeText(txtStartHour));
            int minute = Integer.parseInt(safeText(txtStartMinute));
            int duration = Integer.parseInt(safeText(txtDuration));
            int extensionSeconds = safeText(txtExtensionSeconds).isEmpty() ? 60 : Integer.parseInt(safeText(txtExtensionSeconds));

            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
            if (startTime.isBefore(LocalDateTime.now())) {
                showError("Start time must be now or in the future.");
                return;
            }

            auctionService.createAuction(
                    itemName,
                    itemDesc,
                    startingPrice,
                    minimumJoinAmount,
                    bidStep,
                    startTime,
                    duration,
                    extensionSeconds
            );

            javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            showError("Price, minimum join amount, bid step, time, duration, and extension must be valid numbers.");
        } catch (Exception e) {
            showError("Auction information is invalid.");
        }
    }

    private String safeText(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String safeText(TextArea field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private void showError(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}
