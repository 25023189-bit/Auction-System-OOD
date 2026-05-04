package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;

/**
 * Handler legacy gửi yêu cầu đóng phiên đấu giá.
 *
 * Vai trò:
 * - Bọc lời gọi AuctionService.closeAuction() cho package action cũ.
 * - Tách thao tác UI đóng phòng khỏi service mạng trực tiếp.
 *
 * Luồng chính:
 * 1. UI/presenter gọi closeRoom(roomId) khi seller muốn đóng phiên.
 * 2. Handler kiểm tra service rồi gửi request closeAuction lên server.
 *
 * Business rules:
 * - Chỉ gửi request khi AuctionService đã được gắn.
 * - Quyền đóng phiên cuối cùng do server kiểm tra theo seller sở hữu phòng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: giữ AuctionService mutable.
 * - Dependency: AuctionService.
 */
public class AuctionCloseHandler {
    private AuctionService auctionService;

    public AuctionCloseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void closeRoom(String roomId) {
        if (auctionService != null) {
            auctionService.closeAuction(roomId);
        }
    }
}
