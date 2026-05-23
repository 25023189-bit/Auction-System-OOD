package com.auction.client.feature.controllers.app.response;

import com.auction.client.network.messaging.MessageHandler;
import com.auction.common.dto.Message;

public class LobbyResponseController implements MessageHandler {
    private final MessageHandler delegate;

    public LobbyResponseController(MessageHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supports(String action) {
        return delegate != null && delegate.supports(action);
    }

    @Override
    public void handle(Message msg) {
        if (delegate != null) delegate.handle(msg);
    }
}
