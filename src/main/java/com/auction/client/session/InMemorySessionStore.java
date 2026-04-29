package com.auction.client.session;

import com.auction.client.feature.controllers.AdminController;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

/**
 * Lưu trạng thái phiên làm việc trong bộ nhớ của client.
 * Dữ liệu này mất khi đóng ứng dụng và không ghi xuống database.
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
