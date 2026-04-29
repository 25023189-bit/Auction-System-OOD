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

/**
 * Controller cho popup tạo phiên đấu giá của seller.
 * Thu thập dữ liệu từ form, validate cơ bản và gửi yêu cầu tạo phiên lên server.
 */
public class SellerController {

    @FXML private TextField txtItemName;
    @FXML private TextArea txtItemDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private TextField txtMinimumJoinAmount;
    @FXML private TextField txtBidStep;
    @FXML private Label lblStatus;
    @FXML private Label lblReputation;
    @FXML private DatePicker datePickerStart;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private TextField txtDuration;
    @FXML private TextField txtExtensionSeconds;

    // Service được launcher truyền vào để controller gửi request tạo phiên đấu giá.
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
            // Chuẩn hóa input trước khi parse số và ghép thời điểm bắt đầu.
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

            // Server sẽ quyết định duyệt ngay hay đưa vào danh sách chờ admin phê duyệt.
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

            // Đóng popup sau khi gửi request thành công để người dùng quay về lobby.
            javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            showError("Price, minimum join amount, bid step, time, duration, and extension must be valid numbers.");
        } catch (Exception e) {
            showError("Auction information is invalid.");
        }
    }

    // Tránh NullPointerException khi FXML thiếu field hoặc field chưa có text.
    private String safeText(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    // Phiên bản cho TextArea dùng với mô tả sản phẩm.
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
