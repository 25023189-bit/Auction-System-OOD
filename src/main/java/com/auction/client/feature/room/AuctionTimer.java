package com.auction.client.feature.room;

import java.time.LocalDateTime;

/**
 * Hợp đồng cho service đếm giờ phiên đấu giá trên client.
 *
 * Vai trò:
 * - Chuẩn hóa cách bắt đầu timer theo startTime/endTime.
 * - Cung cấp thao tác stop khi rời phòng hoặc đổi màn hình.
 *
 * Luồng chính:
 * 1. Message handler hoặc controller gọi start() khi nhận AuctionRoom mới.
 * 2. RoomTransitionHandler gọi stop() khi user rời phòng.
 *
 * Business rules:
 * - Timer phải phản ánh cả trạng thái chưa bắt đầu và đã kết thúc.
 * - stop() phải dừng mọi callback UI còn tồn tại.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; DefaultAuctionTimerService dùng JavaFX Timeline nên không thread-safe.
 * - Dependency: LocalDateTime và implementation DefaultAuctionTimerService.
 */
public interface AuctionTimer {
    void start(LocalDateTime startTime, LocalDateTime endTime);
    void stop();
}
