package com.auction.client.feature.controllers.auction.autobid;

import com.auction.client.network.socket.ClientConnection;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.Message;

public class AutoBidController {
    private final ClientConnection clientConnection;

    public AutoBidController(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public void setAutoBid(String roomId, double maxBid, double step) {
        clientConnection.sendMessage(new Message("SET_AUTO_BID", new AutoBidRequest(roomId, maxBid, step)));
    }

    public void cancelAutoBid(String roomId) {
        clientConnection.sendMessage(new Message("CANCEL_AUTO_BID", roomId));
    }
}
