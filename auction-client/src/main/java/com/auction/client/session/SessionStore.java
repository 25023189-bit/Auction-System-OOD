package com.auction.client.session;

import com.auction.client.feature.controllers.account.admin.AdminController;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

/**
 * Hợp đồng lưu trạng thái phiên hiện tại của client.
 *
 * Vai trò:
 * - Chuẩn hóa nơi đọc/ghi user, room và controller phụ đang gắn với phiên.
 * - Tách message handler/presenter khỏi implementation lưu session cụ thể.
 *
 * Luồng chính:
 * 1. Sau login/join/open dashboard, handler cập nhật dữ liệu tương ứng vào SessionStore.
 * 2. Khi logout hoặc account bị khóa, caller gọi clearSession().
 *
 * Business rules:
 * - currentRoomId/currentRoom phải được xóa khi rời phòng hoặc phiên bị đóng.
 * - AdminController chỉ dùng khi dashboard admin đang mở.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; InMemorySessionStore không thread-safe.
 * - Dependency: User, AuctionRoom, AdminController.
 */
public interface SessionStore {
    String getCurrentRoomId();

    void setCurrentRoomId(String roomId);

    AuctionRoom getCurrentRoom();

    void setCurrentRoom(AuctionRoom room);

    User getCurrentUser();

    void setCurrentUser(User user);

    String getCurrentUsername();

    void setCurrentUsername(String username);

    AdminController getAdminController();

    void setAdminController(AdminController adminController);

    void clearSession();
}
