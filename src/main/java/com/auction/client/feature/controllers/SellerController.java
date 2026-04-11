package com.auction.client.feature.controllers;

import com.auction.server.service.AuctionService;
import com.auction.common.model.Seller;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SellerController {

    @FXML private TextField txtItemName;
    @FXML private TextArea txtItemDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private Label lblStatus;

    @FXML private Label lblBalance;
    @FXML private Label lblReputation;
    @FXML private TextField txtRoomIdToClose;

    @FXML private DatePicker datePickerStart;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private TextField txtDuration;

    private AuctionService auctionService;
    private Seller currentSeller;

    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    public void setSellerData(Seller seller) {
        this.currentSeller = seller;
        if (seller != null) {
            lblBalance.setText("Số dư ví: " + String.format("%,.0f VNĐ", seller.getBalance()));
            lblReputation.setText("Uy tín: " + String.format("%.1f ⭐", seller.getRatingScore()));
        }
    }

    @FXML
    public void handleCreateAuction() {
        if (this.auctionService == null) {
            lblStatus.setText("❌ Lỗi: Service bị null!");
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

            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

            if (startTime.isBefore(LocalDateTime.now())) {
                lblStatus.setText("❌ Thời gian bắt đầu phải ở hiện tại hoặc tương lai!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            if (currentSeller != null && !currentSeller.isTrustworthy()) {
                lblStatus.setText("❌ CẢNH BÁO: Uy tín quá thấp!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            auctionService.createAuction(itemName, itemDesc, startingPrice, startTime, duration);

            javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            lblStatus.setText("❌ Giá, Giờ, Phút và Thời lượng phải là số hợp lệ!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        } catch (java.time.DateTimeException e) {
            lblStatus.setText("❌ Thời gian không hợp lệ (Giờ: 0-23, Phút: 0-59)!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleCloseAction(ActionEvent event) {
        String roomId = txtRoomIdToClose.getText().trim();
        if (roomId.isEmpty()) {
            lblStatus.setText("❌ Vui lòng nhập Mã phòng cần chốt đơn!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        if (auctionService != null) {
            auctionService.closeAuction(roomId);
            lblStatus.setText("✅ Đã gửi lệnh chốt đơn cho phòng: " + roomId);
            lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
            txtRoomIdToClose.clear();
        }
    }
}