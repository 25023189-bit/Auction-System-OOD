package com.auction.server.handler;

import com.auction.common.dto.Message;

/**
 * Hợp đồng cho handler xử lý một nhóm action gửi từ client.
 */
public interface ClientActionHandler {
    boolean canHandle(String action);

    void handle(Message message, ClientActionContext context);
}
