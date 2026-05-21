package com.auction.common.model;

public class AutoBidAgent {
    private final String userId;
    private final double maxBid;
    private final double increment;

    public AutoBidAgent(String userId, double maxBid, double increment) {
        this.userId = userId;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public String getUserId() { return userId; }
    public double getMaxBid() { return maxBid; }
    public double getIncrement() { return increment; }
}