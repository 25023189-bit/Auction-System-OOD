package com.auction.client.feature.controllers.app.root;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.core.navigation.WindowStateHandler;
import com.auction.client.network.socket.ClientConnection;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.support.AlertService;
import com.auction.client.shared.support.FxThreadExecutor;
import com.auction.client.shared.support.StageLocator;
import com.auction.client.shared.support.UiResetService;
import com.auction.common.role.RolePolicy;

public record AuctionControllerDependencies(
        ClientConnection clientConnection,
        AuctionService auctionService,
        SessionStore sessionStore,
        WindowStateHandler windowStateHandler,
        RolePolicy rolePolicy,
        SceneNavigator sceneNavigator,
        AlertService alertService,
        StageLocator stageLocator,
        UiResetService uiResetService,
        FxThreadExecutor fxThreadExecutor
) {
}
