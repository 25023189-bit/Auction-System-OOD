package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

public interface ResponseRouter {
    void route(Message message);
}