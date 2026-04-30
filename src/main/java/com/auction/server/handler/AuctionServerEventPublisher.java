package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.main.AuctionServer;

/**
 * Adapter nối handler nghiệp vụ với các hàm static broadcast của AuctionServer.
 */
public class AuctionServerEventPublisher implements ServerEventPublisher {
    @Override
    public void broadcastAll(Message message) {
        AuctionServer.broadcast(message);
    }

    @Override
    public void broadcastToRoom(String roomId, Message message) {
        AuctionServer.broadcastToRoom(roomId, message);
    }

    @Override
    public void notifyRoomClosed(String roomId) {
        AuctionServer.notifyRoomClosed(roomId);
    }

    @Override
    public void notifyDeletedUser(String userId) {
        AuctionServer.notifyDeletedUser(userId);
    }
}
