package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

/**
 * Điều phối phản hồi server đến handler phù hợp.
 */
public interface ResponseRouter {
    void route(Message message);
}
