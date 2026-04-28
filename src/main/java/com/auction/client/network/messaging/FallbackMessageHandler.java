package com.auction.client.network.messaging;

import com.auction.common.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FallbackMessageHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMessageHandler.class);

    private final List<MessageHandler> fallbackHandlers;

    public FallbackMessageHandler(List<MessageHandler> fallbackHandlers) {
        this.fallbackHandlers = fallbackHandlers;
    }

    @Override
    public boolean supports(String action) {
        return true;
    }

    @Override
    public void handle(Message message) {
        for (MessageHandler handler : fallbackHandlers) {
            if (handler.supports(message.getAction())) {
                handler.handle(message);
                return;
            }
        }

        LOGGER.warn("No handler for action: {}", message.getAction());
    }
}
