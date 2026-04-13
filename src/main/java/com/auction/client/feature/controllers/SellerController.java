package com.auction.client.feature.controllers;

import com.auction.server.service.AuctionService;
import com.auction.common.model.Seller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.time.*;

public class SellerController {

    @FXML private TextField txtItemName;
    @FXML private TextArea txtItemDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private Label lblStatus;

    @FXML private Label lblReputation;

    @FXML private DatePicker datePickerStart;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private TextField txtDuration;
    @FXML private TextField txtExtensionSeconds;

    private AuctionService auctionService;
    private Seller currentSeller;
    private int extensionSeconds;

    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    public void setSellerData(Seller seller) {
        this.currentSeller = seller;
        if (seller != null) {
            // ĐÃ FIX: Bỏ tính năng Rating phức tạp, set text tĩnh hoặc ẩn đi
            if(lblReputation != null) {
                lblReputation.setText("Quyền: NGƯỜI BÁN (SELLER)");
            }
        }
    }

    @FXML
    public void handleCreateAuction() {
        if (this.auctionService == null) {
            lblStatus.setText("❌ Lỗi: Service bị null!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        LocalDate date = datePickerStart.getValue();
        if (date == null) {
            lblStatus.setText("❌ Vui lòng chọn ngày bắt đầu!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        String itemName = txtItemName.getText();
        String priceStr = txtStartingPrice.getText();
        String itemDesc = (txtItemDescription != null) ? txtItemDescription.getText() : "";
        String extensionStr = (txtExtensionSeconds != null && txtExtensionSeconds.getText() != null)
                ? txtExtensionSeconds.getText().trim()
                : "";

        if (itemName.trim().isEmpty() || priceStr.trim().isEmpty() || itemDesc.trim().isEmpty()) {
            lblStatus.setText("❌ Vui lòng điền đầy đủ thông tin (kể cả Mô tả)!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            double startingPrice = Double.parseDouble(priceStr);
            if (startingPrice <= 0) {
                lblStatus.setText("❌ Giá khởi điểm phải lớn hơn 0!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            int hour = Integer.parseInt(txtStartHour.getText().trim());
            int minute = Integer.parseInt(txtStartMinute.getText().trim());
            int duration = Integer.parseInt(txtDuration.getText().trim());

            if (duration <= 0) {
                lblStatus.setText("❌ Thời lượng phải lớn hơn 0 phút!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            int parsedExtensionSeconds = extensionStr.isEmpty() ? 60 : Integer.parseInt(extensionStr);

            if (parsedExtensionSeconds < 1 || parsedExtensionSeconds > 120) {
                lblStatus.setText("❌ Thời gian gia hạn mỗi lần phải từ 1 đến 120 giây!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

            if (startTime.isBefore(LocalDateTime.now())) {
                lblStatus.setText("❌ Thời gian bắt đầu phải ở hiện tại hoặc tương lai!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            // ĐÃ FIX: Đã gỡ bỏ đoạn check `!currentSeller.isTrustworthy()` vì không còn phù hợp với DB chuẩn.

            this.extensionSeconds = parsedExtensionSeconds;

            auctionService.createAuction(
                    itemName,
                    itemDesc,
                    startingPrice,
                    startTime,
                    duration,
                    this.extensionSeconds
            );

            javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            lblStatus.setText("❌ Giá, Giờ, Phút, Thời lượng và Gia hạn phải là số hợp lệ!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        } catch (java.time.DateTimeException e) {
            lblStatus.setText("❌ Thời gian không hợp lệ (Giờ: 0-23, Phút: 0-59)!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}