package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

public interface MessageHandler {
    boolean supports(String action);

    void handle(Message message);
}