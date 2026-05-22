package com.auction.client.feature.controllers.app.response;

import com.auction.client.network.dispatcher.MessageRouteResult;
import com.auction.client.network.dispatcher.ResponseDispatcher;
import com.auction.client.network.messaging.ResponseRouter;
import com.auction.common.dto.Message;

public class ClientResponseCoordinator {
    private final ResponseRouter responseRouter;
    private final ResponseDispatcher responseDispatcher;

    public ClientResponseCoordinator(ResponseRouter responseRouter, ResponseDispatcher responseDispatcher) {
        this.responseRouter = responseRouter;
        this.responseDispatcher = responseDispatcher;
    }

    public void dispatch(Message message) {
        MessageRouteResult routeResult = responseRouter.route(message);
        responseDispatcher.dispatch(routeResult);
    }
}
