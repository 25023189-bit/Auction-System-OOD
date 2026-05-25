package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.main.AuctionServer;

/**
 * Adapter phát sự kiện server từ handler ra AuctionServer.
 *
 * Vai trò:
 * - Ẩn các hàm static broadcast/notify của AuctionServer sau interface ServerEventPublisher.
 * - Cho phép ClientActionContext phát event mà không phụ thuộc trực tiếp vào chi tiết server main.
 *
 * Luồng chính:
 * 1. Handler gọi các hàm broadcast/notify thông qua ClientActionContext.
 * 2. Adapter chuyển lời gọi sang AuctionServer.broadcast(), broadcastToRoom(), notifyRoomClosed() hoặc notifyDeletedUser().
 *
 * Business rules:
 * - Event toàn hệ thống gửi cho mọi client còn sống.
 * - Event theo room hoặc user chỉ chuyển tiếp đúng roomId/userId được cung cấp.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, mọi state nằm trong AuctionServer.
 * - Dependency: ServerEventPublisher, AuctionServer, Message.
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
    public void notifyRoomClosed(String roomId, Object payload) {
        AuctionServer.notifyRoomClosed(roomId, payload);
    }

    @Override
    public void notifyDeletedUser(String userId) {
        AuctionServer.notifyDeletedUser(userId);
    }
}
