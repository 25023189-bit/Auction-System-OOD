package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private String itemName;
    private String itemDescription;
    private double currentPrice;
    private String sellerName;
    private String highestBidder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    private long extendedSeconds = 0;
    private boolean entryLocked = false;
    private LocalDateTime scheduledEndTime;

    public AuctionRoom() {}

    public AuctionRoom(String roomId, String itemName, double currentPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
    }

    public AuctionRoom(String roomId, String itemName, double currentPrice, String sellerName) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.sellerName = sellerName;
    }

    // --- CÁC HÀM FIX LỖI Ở AuctionDAO (image_afe5ca.jpg) ---
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    // --- CÁC HÀM FIX LỖI Ở ClientHandler (image_af7d09.jpg, image_af6aa0.jpg) ---
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setActualEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setDurationMinutes(int minutes) {
        if (this.startTime != null) {
            this.endTime = this.startTime.plusMinutes(minutes);
        }
    }

    // --- CÁC HÀM FIX LỖI Ở AuctionRoomStateBinder (image_afe265.jpg) ---
    public String getItemDescription() {
        return (itemDescription != null) ? itemDescription : "Không có mô tả";
    }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public boolean isEntryLocked() { return entryLocked; }
    public void setEntryLocked(boolean entryLocked) { this.entryLocked = entryLocked; }

    // --- FIX LỖI Ở AuctionRoomService & MessageHandler (image_af87f2.jpg, image_afde48.jpg) ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    public String getHighestBidder() { return highestBidder; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public long getExtensionSeconds() { return extendedSeconds; }
    public void setExtendedSeconds(long extendedSeconds) { this.extendedSeconds = extendedSeconds; }

    // --- ALIAS CHO GIAO DIỆN ---
    public String getAuctionId() { return roomId; }
    public String getProductId() { return itemName; }
    public String getNameSeller() { return sellerName; }

    // --- GETTER/SETTER CƠ BẢN ---
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}