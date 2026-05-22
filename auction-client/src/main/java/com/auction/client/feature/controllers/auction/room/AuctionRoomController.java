package com.auction.client.feature.controllers.auction.room;

import com.auction.client.feature.room.AuctionRoomStateBinder;
import com.auction.common.model.AuctionRoom;

public class AuctionRoomController {
    private final AuctionRoomStateBinder stateBinder;

    public AuctionRoomController(AuctionRoomStateBinder stateBinder) {
        this.stateBinder = stateBinder;
    }

    public void bind(AuctionRoom room) {
        stateBinder.bind(room);
    }
}
