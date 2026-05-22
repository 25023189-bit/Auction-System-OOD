package com.auction.client.feature.controllers.app.root;

import com.auction.client.core.navigation.DefaultWindowStateHandler;
import com.auction.client.core.navigation.FxSceneNavigator;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.core.navigation.WindowStateHandler;
import com.auction.client.network.socket.ClientConnection;
import com.auction.client.network.socket.ServerMessageListener;
import com.auction.client.service.AuctionService;
import com.auction.client.session.InMemorySessionStore;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.support.AlertService;
import com.auction.client.shared.support.DefaultFxThreadExecutor;
import com.auction.client.shared.support.DefaultStageLocator;
import com.auction.client.shared.support.FxAlertService;
import com.auction.client.shared.support.FxThreadExecutor;
import com.auction.client.shared.support.StageLocator;
import com.auction.client.shared.support.UiResetService;
import com.auction.common.role.DefaultRolePolicy;
import com.auction.common.role.RolePolicy;

public class AuctionControllerBootstrap {
    public AuctionControllerDependencies bootstrap(ServerMessageListener listener, Object controllerRef) {
        ClientConnection clientConnection = new ClientConnection(listener);
        AuctionService auctionService = new AuctionService(clientConnection);
        clientConnection.connect();

        SessionStore sessionStore = sessionStore(null);
        WindowStateHandler windowStateHandler = windowStateHandler(null);
        RolePolicy rolePolicy = new DefaultRolePolicy();
        SceneNavigator sceneNavigator = sceneNavigator(controllerRef, windowStateHandler, sessionStore, auctionService, null);
        AlertService alertService = new FxAlertService();
        StageLocator stageLocator = new DefaultStageLocator();
        FxThreadExecutor fxThreadExecutor = new DefaultFxThreadExecutor();

        return new AuctionControllerDependencies(
                clientConnection,
                auctionService,
                sessionStore,
                windowStateHandler,
                rolePolicy,
                sceneNavigator,
                alertService,
                stageLocator,
                null,
                fxThreadExecutor
        );
    }

    public SessionStore sessionStore(SessionStore current) {
        return current != null ? current : new InMemorySessionStore();
    }

    public WindowStateHandler windowStateHandler(WindowStateHandler current) {
        return current != null ? current : new DefaultWindowStateHandler();
    }

    public SceneNavigator sceneNavigator(Object controllerRef,
                                         WindowStateHandler windowStateHandler,
                                         SessionStore sessionStore,
                                         AuctionService auctionService,
                                         SceneNavigator current) {
        return current != null ? current : new FxSceneNavigator(controllerRef, windowStateHandler, sessionStore, auctionService);
    }
}
