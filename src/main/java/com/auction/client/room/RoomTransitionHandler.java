package com.auction.client.room;

import com.auction.client.lobby.LobbyUserInfoBinder;
import com.auction.client.navigation.SceneNavigator;
import com.auction.client.session.SessionStore;
import com.auction.server.service.AuctionService;

public class RoomTransitionHandler {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;
    private final SceneNavigator sceneNavigator;
    private final AuctionTimer auctionTimer;
    private final LobbyUserInfoBinder lobbyUserInfoBinder;

    public RoomTransitionHandler(AuctionService auctionService,
                                 SessionStore sessionStore,
                                 SceneNavigator sceneNavigator,
                                 AuctionTimer auctionTimer,
                                 LobbyUserInfoBinder lobbyUserInfoBinder) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.auctionTimer = auctionTimer;
        this.lobbyUserInfoBinder = lobbyUserInfoBinder;
    }

    public void backToLobby() {
        if (auctionTimer != null) {
            auctionTimer.stop();
        }

        if (auctionService != null) {
            auctionService.leaveRoom();
        }

        if (sessionStore != null) {
            sessionStore.setCurrentRoom(null);
            sessionStore.setCurrentRoomId(null);
        }

        if (sceneNavigator != null) {
            sceneNavigator.showLobby();
        }

        if (lobbyUserInfoBinder != null && sessionStore != null) {
            lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
        }

        if (auctionService != null) {
            auctionService.getRooms();
        }
    }
}