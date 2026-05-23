package com.auction.client.feature.controllers.auction.room.lifecycle;

import com.auction.client.feature.room.AuctionTimer;

import java.time.LocalDateTime;

public class AuctionTimerController {
    private final AuctionTimer timer;

    public AuctionTimerController(AuctionTimer timer) {
        this.timer = timer;
    }

    public void start(LocalDateTime startTime, LocalDateTime endTime) {
        timer.start(startTime, endTime);
    }

    public void stop() {
        timer.stop();
    }
}
