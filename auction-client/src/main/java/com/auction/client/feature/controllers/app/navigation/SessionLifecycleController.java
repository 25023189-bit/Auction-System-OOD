package com.auction.client.feature.controllers.app.navigation;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.room.AuctionTimer;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.support.UiResetService;

public class SessionLifecycleController {
    private final SessionStore sessionStore;
    private final AuctionService auctionService;
    private final SceneNavigator sceneNavigator;
    private final UiResetService uiResetService;
    private AuctionTimer auctionTimer;

    public SessionLifecycleController(
            SessionStore sessionStore,
            AuctionService auctionService,
            SceneNavigator sceneNavigator,
            UiResetService uiResetService
    ) {
        this.sessionStore = sessionStore;
        this.auctionService = auctionService;
        this.sceneNavigator = sceneNavigator;
        this.uiResetService = uiResetService;
    }

    public void setAuctionTimer(AuctionTimer auctionTimer) {
        this.auctionTimer = auctionTimer;
    }

    public void openLoginScreen() {
        resetSessionState();
        sceneNavigator.showLogin();
    }

    private void resetSessionState() {
        sessionStore.clearSession();
        if (auctionService != null) auctionService.setCurrentUser(null);
        if (auctionTimer != null) auctionTimer.stop();
        if (uiResetService != null) uiResetService.resetSessionUi();
    }
}
