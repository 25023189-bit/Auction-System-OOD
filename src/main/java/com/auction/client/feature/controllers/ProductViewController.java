package com.auction.client.feature.controllers;

import com.auction.server.service.AuctionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.auction.common.model.AuctionRoom;

public class ProductViewController {

    @FXML private Label lblProductName;
    @FXML private Label lblDescription;
    @FXML private Label lblStartingPrice;

    private AuctionService auctionService;
    private String currentRoomId;

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;

        this.auctionService.setProductDetailsCallback(data -> {
            Platform.runLater(() -> {
                if (data != null) {
                    try {
                        AuctionRoom product = (AuctionRoom) data;

                        if(lblProductName != null) lblProductName.setText(product.getItemName());
                        if(lblDescription != null) lblDescription.setText(product.getItemDescription());
                        if(lblStartingPrice != null) lblStartingPrice.setText(String.format("%,.0f $", product.getStartingPrice()));
                    } catch (Exception e) {
                        if(lblDescription != null) lblDescription.setText(data.toString());
                    }
                } else {
                    // Đổi text báo không tìm thấy
                    if(lblDescription != null) lblDescription.setText("Product information not found!");
                }
            });
        });
    }

    public void setRoomId(String roomId) {
        this.currentRoomId = roomId;
        // Đổi text báo đang tải
        if (lblDescription != null) {
            lblDescription.setText("Loading data from server...");
        }

        if (this.auctionService != null) {
            this.auctionService.requestProductDetails(roomId);
        }
    }
}