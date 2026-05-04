package com.auction.client.feature.viewmodel;

import com.auction.common.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel giữ trạng thái hiển thị của lobby.
 *
 * Vai trò:
 * - Lưu user hiện tại đang xem lobby.
 * - Lưu danh sách LobbyRoomDisplayModel đã chuẩn hóa để render card.
 *
 * Luồng chính:
 * 1. Sau login hoặc ROOM_LIST, controller/handler cập nhật currentUser và rooms.
 * 2. Presenter/renderer đọc dữ liệu để cập nhật header và danh sách phòng.
 *
 * Business rules:
 * - replaceRooms() phải thay toàn bộ danh sách để tránh trộn response cũ và mới.
 * - rooms không được trả null, caller luôn nhận list hiện tại.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: rooms là ArrayList mutable, dùng trong luồng UI.
 * - Dependency: User, LobbyRoomDisplayModel, List/ArrayList.
 */
public class LobbyViewModel {
    private User currentUser;
    private final List<LobbyRoomDisplayModel> rooms = new ArrayList<>();

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public List<LobbyRoomDisplayModel> getRooms() {
        return rooms;
    }

    // Thay toàn bộ danh sách để tránh trộn dữ liệu cũ với response mới.
    public void replaceRooms(List<LobbyRoomDisplayModel> newRooms) {
        rooms.clear();
        if (newRooms != null) {
            rooms.addAll(newRooms);
        }
    }
}
