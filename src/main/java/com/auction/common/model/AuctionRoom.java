package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private String itemName;
    private double currentPrice;
    private String highestBidder;
    private String nameSeller;
    private String highestBidderId = "Chưa có ai";
    private String status;
    private LocalDateTime startTime;
    private int durationMinutes;

    // Chỉ set khi phiên đóng thật sự
    private LocalDateTime actualEndTime;

    // Dùng runtime/client countdown
    private LocalDateTime scheduledEndTime;

    private boolean entryLocked;
    private long extendedSeconds;

    private String itemDescription;

    public AuctionRoom(String roomId, String itemName, double startingPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = startingPrice;
        this.highestBidder = "None";
    }

    public AuctionRoom(String roomId, String itemName, double startingPrice, String nameSeller) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = startingPrice;
        this.nameSeller = nameSeller;
    }

    public String getRoomId() { return roomId; }
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public String getHighestBidderId() { return this.highestBidderId; }
    public String getNameSeller() { return nameSeller; }
    public String getStatus() { return status; }
    public int getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
    public boolean isEntryLocked() { return entryLocked; }
    public long getExtendedSeconds() { return extendedSeconds; }
    public String getItemDescription() { return itemDescription; }

    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public void setStatus(String status) { this.status = status; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    public void setEntryLocked(boolean entryLocked) { this.entryLocked = entryLocked; }
    public void setExtendedSeconds(long extendedSeconds) { this.extendedSeconds = extendedSeconds; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    @Override
    public String toString() {
        return "Room[" + roomId + " - " + itemName + " - Price: " + currentPrice + "]";
    }
}