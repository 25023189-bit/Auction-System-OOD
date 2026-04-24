package com.auction.server.handler;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;

import java.time.LocalDateTime;

public class PendingAuctionRoomFactory {
    public AuctionRoom createRoom(PendingAuctionRequest request) {
        AuctionRoom room = new AuctionRoom(
                request.getRoomId(),
                request.getItemName(),
                request.getStartingPrice(),
                request.getSellerId()
        );
        room.setItemId(request.getItemId());
        room.setItemDescription(request.getItemDesc());
        room.setCurrentPrice(request.getStartingPrice());
        room.setStartingPrice(request.getStartingPrice());
        room.setMinimumJoinAmount(request.getMinimumJoinAmount());
        room.setBidStep(request.getBidStep());
        room.setStartTime(request.getStartTime());
        room.setDurationMinutes(request.getDurationMinutes());
        room.setActualEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
        room.setExtensionSeconds(request.getExtensionSeconds());
        room.setSellerReputation(request.getSellerReputation());
        room.setSellerSuccessfulAuctionRate(request.getSuccessfulAuctionRate());
        room.setSellerAdminCancellationRate(request.getAdminCancellationRate());
        room.setStatus(request.getStartTime().isAfter(LocalDateTime.now()) ? "OPEN" : "RUNNING");
        return room;
    }

    public Item createItem(PendingAuctionRequest request) {
        return new Item(
                request.getItemId(),
                request.getItemName(),
                request.getItemDesc(),
                request.getStartingPrice()
        );
    }
}
