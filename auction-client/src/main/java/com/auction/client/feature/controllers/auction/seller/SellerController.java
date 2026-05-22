package com.auction.client.feature.controllers.auction.seller;

import com.auction.client.feature.controllers.auction.seller.feedback.SellerFormFeedbackController;
import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormData;
import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormReader;
import com.auction.client.feature.controllers.auction.seller.validation.SellerAuctionValidationResult;
import com.auction.client.feature.controllers.auction.seller.validation.SellerAuctionValidator;
import com.auction.client.service.AuctionService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    private SellerAuctionFormReader formReader;
    private SellerAuctionValidator validator;
    private SellerFormFeedbackController feedbackController;

    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    @FXML
    public void initialize() {
        formReader = new SellerAuctionFormReader(
                txtItemName, txtItemDescription, txtStartingPrice, txtMinimumJoinAmount, txtBidStep,
                datePickerStart, txtStartHour, txtStartMinute, txtDuration, txtExtensionSeconds
        );
        validator = new SellerAuctionValidator();
        feedbackController = new SellerFormFeedbackController(lblStatus);

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

    private boolean validateExtensionRealTime() { // Đổi từ void thành boolean
        String extStr = safeText(txtExtensionSeconds);

        if (extStr.isEmpty()) {
            if (lblStatus != null && lblStatus.getText().contains("Extension")) {
                lblStatus.setText("");
            }
            txtExtensionSeconds.setStyle("-fx-border-color: #e67e22;");
            showError("Error: Extension Seconds cannot be empty!"); // Báo lỗi nếu bỏ trống khi bấm tạo
            return false; // Trống là không hợp lệ
        }

        try {
            int extSecs = Integer.parseInt(extStr);
            if (extSecs < 60 || extSecs > 120) {
                showError("Error: Extension Seconds must be between 60 and 120 seconds!");
                txtExtensionSeconds.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                return false; // SAI -> Trả về false để chặn
            } else {
                if (lblStatus != null) lblStatus.setText("");
                txtExtensionSeconds.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1px;");
                return true; // ĐÚNG -> Trả về true để cho qua
            }
        } catch (NumberFormatException e) {
            showError("Error: Extension must be a valid integer number");
            txtExtensionSeconds.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            return false; // SAI -> Trả về false để chặn
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

        try {
            SellerAuctionFormData formData = formReader.read();
            SellerAuctionValidationResult result = validator.validate(formData);
            if (!result.valid()) {
                showError(result.message());
                return;
            }

            double startingPrice = Double.parseDouble(formData.startingPrice());
            double minimumJoinAmount = Double.parseDouble(formData.minimumJoinAmount());
            double bidStep = Double.parseDouble(formData.bidStep());
            int hour = Integer.parseInt(formData.startHour());
            int minute = Integer.parseInt(formData.startMinute());
            int duration = Integer.parseInt(formData.duration());
            int extensionSeconds = formData.extensionSeconds().isEmpty() ? 60 : Integer.parseInt(formData.extensionSeconds());
            LocalDateTime startTime = LocalDateTime.of(formData.startDate(), LocalTime.of(hour, minute));

            auctionService.createAuction(
                    formData.itemName(),
                    formData.itemDescription(),
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
        if (feedbackController == null) feedbackController = new SellerFormFeedbackController(lblStatus);
        feedbackController.showError(message);
    }
}

