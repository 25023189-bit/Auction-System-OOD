package com.auction.client.feature.controllers;

import com.auction.client.service.AuctionService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controller cho popup seller tạo yêu cầu mở phiên đấu giá.
 * Tích hợp kiểm tra Real-time Validation cho tiền cọc, duration và extension.
 */
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
    public void initialize() {
        if (txtStartingPrice != null) {
            txtStartingPrice.textProperty().addListener((observable, oldValue, newValue) -> {
                validateJoinAmountRealTime();
            });
        }

        if (txtMinimumJoinAmount != null) {
            txtMinimumJoinAmount.textProperty().addListener((observable, oldValue, newValue) -> {
                validateJoinAmountRealTime();
            });
        }

        if (txtExtensionSeconds != null) {
            txtExtensionSeconds.textProperty().addListener((observable, oldValue, newValue) -> {
                validateExtensionRealTime();
            });
        }

        if (txtDuration != null) {
            txtDuration.textProperty().addListener((observable, oldValue, newValue) -> {
                validateDurationRealTime();
            });
        }
    }

    private void validateJoinAmountRealTime() {
        try {
            String startPriceStr = safeText(txtStartingPrice);
            String joinAmountStr = safeText(txtMinimumJoinAmount);

            if (startPriceStr.isEmpty() || joinAmountStr.isEmpty()) {
                if (lblStatus != null && lblStatus.getText().contains("75%")) lblStatus.setText("");
                txtMinimumJoinAmount.setStyle("-fx-border-color: #e67e22;");
                return;
            }

            double startPrice = Double.parseDouble(startPriceStr);
            double joinAmount = Double.parseDouble(joinAmountStr);

            if (joinAmount >= 0.75 * startPrice) {
                showError("Error: Minimum join amount must be < 75% of starting price");
                txtMinimumJoinAmount.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                if (lblStatus != null) lblStatus.setText("");
                txtMinimumJoinAmount.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1px;");
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void validateExtensionRealTime() {
        String extStr = safeText(txtExtensionSeconds);

        if (extStr.isEmpty()) {
            if (lblStatus != null && lblStatus.getText().contains("Extension")) {
                lblStatus.setText("");
            }
            txtExtensionSeconds.setStyle("-fx-border-color: #e67e22;");
            return;
        }

        try {
            int extSecs = Integer.parseInt(extStr);
            if (extSecs <= 0) {
                showError("Error: Extension time must be greater than 0");
                txtExtensionSeconds.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                if (lblStatus != null) lblStatus.setText("");
                txtExtensionSeconds.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1px;");
            }
        } catch (NumberFormatException e) {
            showError("Error: Extension must be a valid integer number");
            txtExtensionSeconds.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    private void validateDurationRealTime() {
        String durationStr = safeText(txtDuration);

        if (durationStr.isEmpty()) {
            if (lblStatus != null && lblStatus.getText().contains("Duration")) {
                lblStatus.setText("");
            }
            txtDuration.setStyle("-fx-border-color: #e67e22;");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                showError("Error: Duration must be greater than 0 minutes");
                txtDuration.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                if (lblStatus != null) lblStatus.setText("");
                txtDuration.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1px;");
            }
        } catch (NumberFormatException e) {
            showError("Error: Duration must be a valid integer number");
            txtDuration.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
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

            // CHỐT CHẶN CUỐI CÙNG
            if (minimumJoinAmount >= 0.75 * startingPrice) {
                showError("Cannot create: Minimum join amount must be < 75% of starting price.");
                return;
            }
            if (extensionSeconds <= 0) {
                showError("Cannot create: Extension time must be greater than 0.");
                return;
            }
            if (duration <= 0) {
                showError("Cannot create: Duration must be greater than 0 minutes.");
                return;
            }

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