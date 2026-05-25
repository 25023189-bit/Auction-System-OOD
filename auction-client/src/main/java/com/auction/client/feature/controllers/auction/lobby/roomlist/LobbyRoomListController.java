package com.auction.client.feature.controllers.auction.lobby.roomlist;

import com.auction.client.feature.lobby.LobbyRoomListRenderer;
import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;

import java.util.List;

public class LobbyRoomListController {
    private final LobbyRoomListRenderer renderer;

    public LobbyRoomListController(LobbyRoomListRenderer renderer) {
        this.renderer = renderer;
    }

    public void render(List<LobbyRoomDisplayModel> rooms) {
        renderer.render(rooms);
    }
}
