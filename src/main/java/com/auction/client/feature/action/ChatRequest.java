package com.auction.client.feature.action;

/**
 * Request object for chat messages
 */
public class ChatRequest {
    private String roomId;
    private String message;
    private String userId;

    public ChatRequest(String roomId, String message, String userId) {
        this.roomId = roomId;
        this.message = message;
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }
}
