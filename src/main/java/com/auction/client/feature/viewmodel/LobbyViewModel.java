package com.auction.client.feature.viewmodel;

import com.auction.common.model.User;

import java.util.ArrayList;
import java.util.List;

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

    public void replaceRooms(List<LobbyRoomDisplayModel> newRooms) {
        rooms.clear();
        if (newRooms != null) {
            rooms.addAll(newRooms);
        }
    }
}