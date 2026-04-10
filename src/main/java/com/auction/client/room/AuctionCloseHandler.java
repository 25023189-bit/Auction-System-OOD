package com.auction.client.room;

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
                "Phòng đấu giá đã được đóng. Chúc mừng bạn đã bán thành công!");
        alert.setHeaderText("Chốt đơn thành công");
        alert.showAndWait();
    }
}