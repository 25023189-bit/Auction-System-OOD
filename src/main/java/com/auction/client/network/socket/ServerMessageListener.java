package com.auction.client.network.socket;

import com.auction.common.dto.Message;

public interface ServerMessageListener {
    void onServerResponse(Message message);

    void updateConnectionStatus(String status);
}
