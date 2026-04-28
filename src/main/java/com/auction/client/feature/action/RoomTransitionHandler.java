package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.presenter.AuctionTimerService;
import com.auction.client.feature.presenter.LobbyUserInfoBinder;

/**
 * Handles transitions between auction rooms
 */
public class RoomTransitionHandler {
    private AuctionService auctionService;
    private SessionStore sessionStore;
    private SceneNavigator sceneNavigator;
    private AuctionTimerService timerService;
    private LobbyUserInfoBinder userInfoBinder;

    public RoomTransitionHandler(AuctionService auctionService, SessionStore sessionStore,
                                 SceneNavigator sceneNavigator, AuctionTimerService timerService,
                                 LobbyUserInfoBinder userInfoBinder) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.timerService = timerService;
        this.userInfoBinder = userInfoBinder;
    }

    public void joinRoom(String roomId) {
        if (auctionService != null) {
            auctionService.joinRoom(roomId);
        }
    }

    public void leaveRoom(String roomId) {
        if (auctionService != null) {
            auctionService.leaveRoom(); // Xóa tham số roomId
        }
        if (timerService != null) {
            timerService.stop();
        }
    }

    // Bổ sung method backToLobby() mà AuctionController đang gọi
    public void backToLobby() {
        if (sessionStore != null) {
            String currentRoomId = sessionStore.getCurrentRoomId();
            if (currentRoomId != null) {
                leaveRoom(currentRoomId);
            }
        }
    }
}