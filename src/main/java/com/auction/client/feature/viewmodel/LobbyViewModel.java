package com.auction.client.feature.viewmodel;

import com.auction.common.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel giữ user hiện tại và danh sách phòng đã chuẩn hóa cho lobby.
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
