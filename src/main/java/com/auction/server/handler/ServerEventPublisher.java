package com.auction.server.handler;

import com.auction.common.dto.Message;

public interface ServerEventPublisher {
    void broadcastAll(Message message);

    void broadcastToRoom(String roomId, Message message);

    void notifyRoomClosed(String roomId);

    void notifyDeletedUser(String userId);
}
