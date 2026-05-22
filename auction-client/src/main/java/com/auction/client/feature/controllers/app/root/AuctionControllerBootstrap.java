package com.auction.client.feature.controllers.app.root;

import com.auction.client.core.navigation.DefaultWindowStateHandler;
import com.auction.client.core.navigation.FxSceneNavigator;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.core.navigation.WindowStateHandler;
import com.auction.client.service.AuctionService;
import com.auction.client.session.InMemorySessionStore;
import com.auction.client.session.SessionStore;

public class AuctionControllerBootstrap {
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
