package com.auction.client.messaging;

import com.auction.common.dto.Message;

import java.util.List;

public class FallbackMessageHandler implements MessageHandler {
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

        System.out.println("⚠️ Chưa có handler cho action: " + message.getAction());
    }
}