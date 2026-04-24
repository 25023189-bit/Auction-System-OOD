package com.auction.server.handler;

import com.auction.common.dto.Message;

public interface ClientActionHandler {
    boolean canHandle(String action);

    void handle(Message message, ClientActionContext context);
}
