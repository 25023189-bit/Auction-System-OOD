package com.auction.server.handler;

import com.auction.common.dto.Message;

/**
 * Hợp đồng phát sự kiện từ handler ra các client khác.
 *
 * Vai trò:
 * - Tách handler nghiệp vụ khỏi cơ chế broadcast cụ thể của server.
 * - Chuẩn hóa các loại event: toàn hệ thống, theo room, đóng room và xóa user.
 *
 * Luồng chính:
 * 1. ClientActionContext nhận Message/event từ handler.
 * 2. Implementation chuyển event tới tầng server chịu trách nhiệm gửi socket.
 *
 * Business rules:
 * - broadcastToRoom chỉ áp dụng cho client đang ở đúng room.
 * - notifyDeletedUser phải ưu tiên thông báo và ngắt kết nối user bị xóa nếu đang online.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: Message và lớp implementation như AuctionServerEventPublisher.
 */
public interface ServerEventPublisher {
    void broadcastAll(Message message);

    void broadcastToRoom(String roomId, Message message);

    void notifyRoomClosed(String roomId);

    void notifyDeletedUser(String userId);
}
