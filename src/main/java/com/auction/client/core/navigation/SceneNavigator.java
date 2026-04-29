package com.auction.client.core.navigation;

import com.auction.common.model.AuctionRoom;

public interface SceneNavigator {
    void showLogin();

    void showLobby();

    void showAuctionRoom(AuctionRoom room);

    void openSellerDashboard();

    void openAdminDashboard();
}