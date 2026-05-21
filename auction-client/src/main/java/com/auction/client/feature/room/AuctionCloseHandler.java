package com.auction.client.feature.room;

import com.auction.client.service.AuctionService;

/**
 * Handler gửi yêu cầu đóng phiên đấu giá từ màn hình phòng.
 *
 * Vai trò:
 * - Bọc lời gọi AuctionService.closeAuction(roomId).
 * - Chặn request đóng phòng khi chưa có roomId hợp lệ.
 *
 * Luồng chính:
 * 1. AuctionController gọi closeRoom(roomId) khi seller bấm nút đóng phiên.
 * 2. Handler validate roomId rồi gửi CLOSE_AUCTION qua AuctionService.
 *
 * Business rules:
 * - Không gửi request nếu roomId null hoặc rỗng.
 * - Quyền đóng phiên theo seller sở hữu phòng do server kiểm tra cuối cùng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: dependency final nhưng AuctionService bên trong có state client.
 * - Dependency: AuctionService.
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
