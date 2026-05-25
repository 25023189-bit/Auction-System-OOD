package com.auction.client.feature.controllers.app.response;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.feature.room.AuctionRoomPresenter;
import com.auction.client.network.dispatcher.AuctionMessageDispatcher;
import com.auction.client.network.dispatcher.MessageRouteResult;
import com.auction.client.network.dispatcher.ResponseDispatcher;
import com.auction.client.network.messaging.AccountStatusFallbackHandler;
import com.auction.client.network.messaging.AdminFallbackHandler;
import com.auction.client.network.messaging.AuctionFlowFallbackHandler;
import com.auction.client.network.messaging.AuctionMessageRouter;
import com.auction.client.network.messaging.BalanceFallbackHandler;
import com.auction.client.network.messaging.FallbackMessageHandler;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.network.messaging.ProductResponseHandler;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.support.FxThreadExecutor;
import com.auction.common.dto.Message;

import java.util.ArrayList;
import java.util.List;

public class ClientResponseCoordinator {
    private final AuctionService auctionService;
    private final FxThreadExecutor fxThreadExecutor;
    private final AuctionMessageRouter responseRouter;
    private final ResponseDispatcher responseDispatcher;

    public ClientResponseCoordinator(
            AuctionService auctionService,
            FxThreadExecutor fxThreadExecutor,
            MessageHandler authMessageHandler,
            MessageHandler lobbyMessageHandler,
            MessageHandler auctionRoomMessageHandler,
            SessionStore sessionStore,
            SceneNavigator sceneNavigator,
            LobbyUserInfoBinder lobbyUserInfoBinder,
            AuctionRoomPresenter auctionRoomPresenter
    ) {
        this.auctionService = auctionService;
        this.fxThreadExecutor = fxThreadExecutor;
        this.responseRouter = buildRouter(
                auctionService,
                authMessageHandler,
                lobbyMessageHandler,
                auctionRoomMessageHandler,
                sessionStore,
                sceneNavigator,
                lobbyUserInfoBinder,
                auctionRoomPresenter
        );
        this.responseDispatcher = new AuctionMessageDispatcher();
    }

    public void handle(Message message) {
        if (message == null) {
            return;
        }

        fxThreadExecutor.execute(() -> dispatch(message));
    }

    private void dispatch(Message message) {
        MessageRouteResult routeResult = responseRouter.route(message);
        responseDispatcher.dispatch(routeResult);
    }

    private AuctionMessageRouter buildRouter(
            AuctionService auctionService,
            MessageHandler authMessageHandler,
            MessageHandler lobbyMessageHandler,
            MessageHandler auctionRoomMessageHandler,
            SessionStore sessionStore,
            SceneNavigator sceneNavigator,
            LobbyUserInfoBinder lobbyUserInfoBinder,
            AuctionRoomPresenter auctionRoomPresenter
    ) {
        List<MessageHandler> primaryHandlers = new ArrayList<>();
        if (authMessageHandler != null) {
            primaryHandlers.add(authMessageHandler);
        }
        if (lobbyMessageHandler != null) {
            primaryHandlers.add(lobbyMessageHandler);
        }
        if (auctionRoomMessageHandler != null) {
            primaryHandlers.add(auctionRoomMessageHandler);
        }
        primaryHandlers.add(new ProductResponseHandler(auctionService));

        List<MessageHandler> fallbackHandlers = new ArrayList<>();
        fallbackHandlers.add(new AuctionFlowFallbackHandler(
                auctionService,
                sessionStore,
                sceneNavigator,
                lobbyUserInfoBinder,
                auctionRoomPresenter
        ));
        fallbackHandlers.add(new BalanceFallbackHandler(auctionService, sessionStore, lobbyUserInfoBinder));
        fallbackHandlers.add(new AdminFallbackHandler(sessionStore));
        fallbackHandlers.add(new AccountStatusFallbackHandler(sceneNavigator, sessionStore, auctionService));

        return new AuctionMessageRouter(primaryHandlers, new FallbackMessageHandler(fallbackHandlers));
    }
}
