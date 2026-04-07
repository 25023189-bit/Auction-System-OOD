package com.auction.client.controllers;

import com.auction.server.service.AuctionService;
import com.auction.common.model.Seller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;

public class SellerController {

    @FXML private TextField txtItemName;
    @FXML private TextArea txtItemDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private Label lblStatus;

    @FXML private Label lblBalance;
    @FXML private Label lblReputation;
    @FXML private TextField txtRoomIdToClose;

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

            if (currentSeller != null && !currentSeller.isTrustworthy()) {
                lblStatus.setText("❌ CẢNH BÁO: Uy tín quá thấp!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            if (auctionService != null) {
                auctionService.createAuction(itemName, itemDesc, startingPrice);

                javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
                stage.close();
            }

        } catch (NumberFormatException e) {
            lblStatus.setText("❌ Giá khởi điểm phải là số!");
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