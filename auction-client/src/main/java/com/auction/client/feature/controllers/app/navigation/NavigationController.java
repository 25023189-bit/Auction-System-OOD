package com.auction.client.feature.controllers.app.navigation;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.common.model.AuctionRoom;

public class NavigationController {
    private final SceneNavigator sceneNavigator;

    public NavigationController(SceneNavigator sceneNavigator) {
        this.sceneNavigator = sceneNavigator;
    }

    public void showLogin() {
        sceneNavigator.showLogin();
    }

    public void showLobby() {
        sceneNavigator.showLobby();
    }

    public void showAuctionRoom(AuctionRoom room) {
        sceneNavigator.showAuctionRoom(room);
    }
}
