package com.auction.client.viewmodel;

import java.time.LocalDateTime;

public class AuctionRoomViewModel {
    private String roomId;
    private String itemName;
    private String itemDescription;
    private double currentPrice;
    private String displayPrice;
    private String sellerName;
    private boolean owner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        this.displayPrice = String.format("%,.0f $", currentPrice);
    }

    public String getDisplayPrice() { return displayPrice; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public boolean isOwner() { return owner; }
    public void setOwner(boolean owner) { this.owner = owner; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}