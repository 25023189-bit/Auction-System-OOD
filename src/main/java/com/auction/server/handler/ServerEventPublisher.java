package com.auction.server.handler;

import com.auction.common.dto.Message;

/**
 * Hợp đồng phát sự kiện từ handler ra các client khác.
 */
public interface ServerEventPublisher {
    void broadcastAll(Message message);

    void broadcastToRoom(String roomId, Message message);

    void notifyRoomClosed(String roomId);

    void notifyDeletedUser(String userId);
}
