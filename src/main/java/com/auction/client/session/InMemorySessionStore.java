package com.auction.client.session;

import com.auction.client.feature.controllers.AdminController;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

/**
 * SessionStore lưu trạng thái phiên làm việc trong bộ nhớ client.
 *
 * Vai trò:
 * - Giữ user hiện tại, room hiện tại và AdminController đang mở.
 * - Cung cấp điểm xóa toàn bộ session khi logout hoặc tài khoản bị khóa.
 *
 * Luồng chính:
 * 1. AuthMessageHandler lưu user sau login, room handler lưu room sau khi join.
 * 2. Các presenter/handler đọc session để bind UI hoặc lọc message theo ngữ cảnh.
 *
 * Business rules:
 * - Dữ liệu session chỉ tồn tại trong tiến trình client, không persist xuống database.
 * - clearSession() phải xóa cả user, room, username và admin controller để tránh dùng dữ liệu cũ.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: field mutable, thường được truy cập từ JavaFX thread/handler đã dispatch.
 * - Dependency: SessionStore, User, AuctionRoom, AdminController.
 */
public class InMemorySessionStore implements SessionStore {
    private String currentRoomId;
    private AuctionRoom currentRoom;
    private User currentUser;
    private String currentUsername = "";
    // Lưu controller admin đang mở để handler có thể cập nhật bảng từ response server.
    private AdminController adminController;

    @Override
    public String getCurrentRoomId() {
        return currentRoomId;
    }

    @Override
    public void setCurrentRoomId(String roomId) {
        this.currentRoomId = roomId;
    }

    @Override
    public AuctionRoom getCurrentRoom() {
        return currentRoom;
    }

    @Override
    public void setCurrentRoom(AuctionRoom room) {
        this.currentRoom = room;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public String getCurrentUsername() {
        return currentUsername;
    }

    @Override
    public void setCurrentUsername(String username) {
        this.currentUsername = username != null ? username : "";
    }

    @Override
    public AdminController getAdminController() {
        return adminController;
    }

    @Override
    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    @Override
    public void clearSession() {
        // Dùng khi logout hoặc tài khoản bị khóa để tránh giữ lại dữ liệu user cũ.
        this.currentRoomId = null;
        this.currentRoom = null;
        this.currentUser = null;
        this.currentUsername = "";
        this.adminController = null;
    }
}
