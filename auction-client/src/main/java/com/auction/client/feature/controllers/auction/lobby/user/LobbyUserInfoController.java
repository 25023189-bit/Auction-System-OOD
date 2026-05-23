package com.auction.client.feature.controllers.auction.lobby.user;

import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.common.model.User;

public class LobbyUserInfoController {
    private final LobbyUserInfoBinder binder;

    public LobbyUserInfoController(LobbyUserInfoBinder binder) {
        this.binder = binder;
    }

    public void bind(User user) {
        binder.bind(user);
    }
}
