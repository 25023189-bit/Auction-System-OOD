package com.auction.client.feature.controllers.auction.room.chat;

import com.auction.client.feature.room.ChatActionHandler;
import com.auction.client.feature.room.ChatRequest;

public class RoomChatController {
    private final ChatActionHandler chatActionHandler;

    public RoomChatController(ChatActionHandler chatActionHandler) {
        this.chatActionHandler = chatActionHandler;
    }

    public void send(String message) {
        chatActionHandler.handle(new ChatRequest(message));
    }
}
