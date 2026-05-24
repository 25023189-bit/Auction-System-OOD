package com.auction.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionRoom implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomId;
    private String itemId;
    private String itemName;
    private String itemDescription;

    private double currentPrice;
    private double startingPrice;
    private double bidStep;
    private double minimumJoinAmount;
    private String sellerName;
    private String highestBidder;
    private int participantCount;
    private double sellerReputation;
    private double sellerSuccessfulAuctionRate;
    private double sellerAdminCancellationRate;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private int extensionSeconds;
    private String status;

    private long extendedSeconds = 0;
    private boolean entryLocked = false;
    private LocalDateTime scheduledEndTime;

    private String base64Image;

    public AuctionRoom() {
    }

    public AuctionRoom(String roomId, String itemName, double currentPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.startingPrice = currentPrice;
    }

    public AuctionRoom(String roomId, String itemName, double currentPrice, String sellerName) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.startingPrice = currentPrice;
        this.sellerName = sellerName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getAuctionId() {
        return roomId;
    }

    public void setAuctionId(String auctionId) {
        this.roomId = auctionId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getProductId() {
        return itemId;
    }

    public void setProductId(String productId) {
        this.itemId = productId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return (itemDescription != null && !itemDescription.isBlank()) ? itemDescription : "Khong co mo ta";
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getBidStep() {
        return bidStep;
    }

    public void setBidStep(double bidStep) {
        this.bidStep = bidStep;
    }

    public double getMinimumJoinAmount() {
        return minimumJoinAmount;
    }

    public void setMinimumJoinAmount(double minimumJoinAmount) {
        this.minimumJoinAmount = minimumJoinAmount;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getNameSeller() {
        return sellerName;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public double getSellerReputation() {
        return sellerReputation;
    }

    public void setSellerReputation(double sellerReputation) {
        this.sellerReputation = sellerReputation;
    }

    public double getSellerSuccessfulAuctionRate() {
        return sellerSuccessfulAuctionRate;
    }

    public void setSellerSuccessfulAuctionRate(double sellerSuccessfulAuctionRate) {
        this.sellerSuccessfulAuctionRate = sellerSuccessfulAuctionRate;
    }

    public double getSellerAdminCancellationRate() {
        return sellerAdminCancellationRate;
    }

    public void setSellerAdminCancellationRate(double sellerAdminCancellationRate) {
        this.sellerAdminCancellationRate = sellerAdminCancellationRate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setActualEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getActualEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getExtensionSeconds() {
        return extensionSeconds;
    }

    public void setExtensionSeconds(int extensionSeconds) {
        this.extensionSeconds = extensionSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEntryLocked() {
        return entryLocked;
    }

    public void setEntryLocked(boolean entryLocked) {
        this.entryLocked = entryLocked;
    }

    public long getExtendedSeconds() {
        return extendedSeconds;
    }

    public void setExtendedSeconds(long extendedSeconds) {
        this.extendedSeconds = extendedSeconds;
    }

    public LocalDateTime getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(LocalDateTime scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public String getBase64Image() { return base64Image; }

    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
}
