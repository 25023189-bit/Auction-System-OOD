package com.auction.client.feature.room;

import com.auction.server.service.AuctionService;

/**
 * Gửi yêu cầu đóng phiên đấu giá từ seller sở hữu phòng.
 */
public class AuctionCloseHandler {
    private final AuctionService auctionService;

    public AuctionCloseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void closeRoom(String roomId) {
        // Không gửi request nếu chưa xác định được phòng hiện tại.
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        auctionService.closeAuction(roomId);
    }
}
