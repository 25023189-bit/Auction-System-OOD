package com.auction.server.handler;

import com.auction.common.dto.Message;

import java.util.List;

public class ClientActionRouter {
    private final List<ClientActionHandler> handlers;

    public ClientActionRouter(List<ClientActionHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public void route(Message message, ClientActionContext context) {
        for (ClientActionHandler handler : handlers) {
            if (handler.canHandle(message.getAction())) {
                handler.handle(message, context);
                return;
            }
        }

        context.send(new Message(
                "UNKNOWN_ACTION",
                "SERVER",
                "Unsupported action: " + message.getAction()
        ));
    }
}
