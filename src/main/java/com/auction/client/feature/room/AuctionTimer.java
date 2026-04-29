package com.auction.client.feature.room;

import java.time.LocalDateTime;

/**
 * Hợp đồng cho service đếm giờ phiên đấu giá.
 */
public interface AuctionTimer {
    void start(LocalDateTime startTime, LocalDateTime endTime);
    void stop();
}
