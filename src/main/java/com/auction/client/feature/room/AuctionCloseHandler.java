package com.auction.client.feature.room;

import com.auction.server.service.AuctionService;
import javafx.scene.control.Alert;

public class AuctionCloseHandler {
    private final AuctionService auctionService;

    public AuctionCloseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void closeRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        auctionService.closeAuction(roomId);

        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Phiên đấu giá đã được đóng bởi người bán.");
        alert.setHeaderText("Đóng phiên thành công");
        alert.showAndWait();
    }
}