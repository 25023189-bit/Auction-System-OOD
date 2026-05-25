package com.auction.common.dto;

import java.io.Serial;
import java.io.Serializable;

public class AuctionEndNotificationPayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String auctionId;
    private String itemName;
    private double finalPrice;
    private String winnerId;
    private String winnerUsername;
    private String sellerId;
    private String sellerUsername;
    private String finalStatus;
    private String endReason;
    private boolean hasWinner;
    private boolean transactionApplied;

    public AuctionEndNotificationPayload() {
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public boolean isHasWinner() {
        return hasWinner;
    }

    public void setHasWinner(boolean hasWinner) {
        this.hasWinner = hasWinner;
    }

    public boolean isTransactionApplied() {
        return transactionApplied;
    }

    public void setTransactionApplied(boolean transactionApplied) {
        this.transactionApplied = transactionApplied;
    }
}
