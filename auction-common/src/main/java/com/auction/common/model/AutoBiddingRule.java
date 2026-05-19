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

    // --- FULL GETTERS VÀ SETTERS Ở ĐÂY ĐỂ BÀI TEST CHẠY ĐƯỢC ---

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(BigDecimal maxBid) {
        this.maxBid = maxBid;
    }

    public BigDecimal getIncrementStep() {
        return incrementStep;
    }

    public void setIncrementStep(BigDecimal incrementStep) {
        this.incrementStep = incrementStep;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}