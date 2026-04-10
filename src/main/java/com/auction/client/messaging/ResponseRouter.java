package com.auction.client.messaging;

import com.auction.common.dto.Message;

public interface ResponseRouter {
    void route(Message message);
}