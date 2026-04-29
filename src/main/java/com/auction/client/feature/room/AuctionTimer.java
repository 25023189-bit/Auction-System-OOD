package com.auction.client.feature.room;

import java.time.LocalDateTime;

public interface AuctionTimer {
    void start(LocalDateTime startTime, LocalDateTime endTime);

    void stop();
}