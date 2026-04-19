package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

import java.util.List;

public class AuctionMessageRouter implements ResponseRouter {
    private final List<MessageHandler> handlers;
    private final MessageHandler fallbackHandler;

    public AuctionMessageRouter(List<MessageHandler> handlers, MessageHandler fallbackHandler) {
        this.handlers = handlers;
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public void route(Message message) {
        for (MessageHandler handler : handlers) {
            if (handler.supports(message.getAction())) {
                handler.handle(message);
                return;
            }
        }

        if (fallbackHandler != null) {
            fallbackHandler.handle(message);
        }
    }
}