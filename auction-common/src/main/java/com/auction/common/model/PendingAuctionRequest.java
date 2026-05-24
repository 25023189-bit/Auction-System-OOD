package com.auction.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class PendingAuctionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String requestId;
    private String roomId;
    private String itemId;
    private String sellerId;
    private String sellerOrganization;
    private String itemName;
    private String itemDesc;
    private double startingPrice;
    private double minimumJoinAmount;
    private double bidStep;
    private LocalDateTime startTime;
    private int durationMinutes;
    private int extensionSeconds;
    private double sellerReputation;
    private double successfulAuctionRate;
    private double adminCancellationRate;

    // --- THÊM BIẾN CHỨA ẢNH ---
    private String base64Image;

    public PendingAuctionRequest() {
    }

    // --- CẬP NHẬT CONSTRUCTOR THÊM BASE64IMAGE ---
    public PendingAuctionRequest(String requestId,
                                 String roomId,
                                 String itemId,
                                 String sellerId,
                                 String sellerOrganization,
                                 String itemName,
                                 String itemDesc,
                                 double startingPrice,
                                 double minimumJoinAmount,
                                 double bidStep,
                                 LocalDateTime startTime,
                                 int durationMinutes,
                                 int extensionSeconds,
                                 double sellerReputation,
                                 double successfulAuctionRate,
                                 double adminCancellationRate,
                                 String base64Image) { // <-- Nhận ảnh ở đây
        this.requestId = requestId;
        this.roomId = roomId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.sellerOrganization = sellerOrganization;
        this.itemName = itemName;
        this.itemDesc = itemDesc;
        this.startingPrice = startingPrice;
        this.minimumJoinAmount = minimumJoinAmount;
        this.bidStep = bidStep;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.extensionSeconds = extensionSeconds;
        this.sellerReputation = sellerReputation;
        this.successfulAuctionRate = successfulAuctionRate;
        this.adminCancellationRate = adminCancellationRate;
        this.base64Image = base64Image; // <-- Lưu ảnh vào biến
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerOrganization() {
        return sellerOrganization;
    }

    public void setSellerOrganization(String sellerOrganization) {
        this.sellerOrganization = sellerOrganization;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getMinimumJoinAmount() {
        return minimumJoinAmount;
    }

    public void setMinimumJoinAmount(double minimumJoinAmount) {
        this.minimumJoinAmount = minimumJoinAmount;
    }

    public double getBidStep() {
        return bidStep;
    }

    public void setBidStep(double bidStep) {
        this.bidStep = bidStep;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
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

    public double getSellerReputation() {
        return sellerReputation;
    }

    public void setSellerReputation(double sellerReputation) {
        this.sellerReputation = sellerReputation;
    }

    public double getSuccessfulAuctionRate() {
        return successfulAuctionRate;
    }

    public void setSuccessfulAuctionRate(double successfulAuctionRate) {
        this.successfulAuctionRate = successfulAuctionRate;
    }

    public double getAdminCancellationRate() {
        return adminCancellationRate;
    }

    public void setAdminCancellationRate(double adminCancellationRate) {
        this.adminCancellationRate = adminCancellationRate;
    }

    // --- GETTER & SETTER CHO ẢNH ---
    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }
}