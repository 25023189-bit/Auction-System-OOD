package com.auction.client.feature.action;

/**
 * Request object for bid placement
 */
public class BidRequest {
    private String roomId;
    private double amount;
    private String userId;

    public BidRequest(String roomId, double amount, String userId) {
        this.roomId = roomId;
        this.amount = amount;
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public double getAmount() {
        return amount;
    }

    public String getUserId() {
        return userId;
    }
}
