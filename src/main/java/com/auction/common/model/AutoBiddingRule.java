package com.auction.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutoBiddingRule {
    private int ruleId;
    private String auctionId;
    private String bidderId;
    private BigDecimal maxBid;
    private BigDecimal incrementStep;
    private boolean isActive = true;
    private LocalDateTime registeredAt;

    public AutoBiddingRule(String auctionId, String bidderId, BigDecimal maxBid, BigDecimal step) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.incrementStep = step;
        this.registeredAt = LocalDateTime.now();
    }

    // Getters/Setters...
}