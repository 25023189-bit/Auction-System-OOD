package com.auction.client.core.navigation;

import com.auction.common.model.AuctionRoom;

/**
 * Hợp đồng điều hướng giữa các màn hình chính của client.
 */
public interface SceneNavigator {
    void showLogin();
    void showLobby();
    void showAuctionRoom(AuctionRoom room);
    void openSellerDashboard();
    void openAdminDashboard();
}
