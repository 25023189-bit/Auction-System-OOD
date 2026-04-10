package com.auction.client.messaging;

import com.auction.common.dto.Message;

public interface MessageHandler {
    boolean supports(String action);
    void handle(Message message);
}