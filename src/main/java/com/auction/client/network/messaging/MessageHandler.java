package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

/**
 * Hợp đồng xử lý một nhóm Message từ server dựa trên action.
 */
public interface MessageHandler {
    boolean supports(String action);

    void handle(Message message);
}
