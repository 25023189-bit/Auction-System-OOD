package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    // ===== ID / THÔNG TIN CƠ BẢN =====
    private String roomId;              // auction_id
    private String itemId;              // item_id
    private String itemName;            // items.name
    private String itemDescription;     // items.description

    // ===== GIÁ / NGƯỜI LIÊN QUAN =====
    private double currentPrice;        // items.current_price
    private String sellerName;          // seller_id hoặc username seller nếu có map thêm
    private String highestBidder;       // bidder hiện tại cao nhất (nếu cần hiển thị)

    // ===== THỜI GIAN / TRẠNG THÁI =====
    private LocalDateTime startTime;        // auctions.start_time
    private LocalDateTime endTime;          // auctions.actual_end_time
    private int durationMinutes;            // auctions.duration_minutes
    private int extensionSeconds;           // auctions.extension_seconds
    private String status;                  // OPEN / SOLD / UNSOLD / ENDED / CANCELED_BY_ADMIN ...

    // ===== RUNTIME STATE (ANTI-SNIPING / CLIENT VIEW) =====
    private long extendedSeconds = 0;       // tổng số giây đã được cộng thêm lúc runtime
    private boolean entryLocked = false;    // khóa người mới vào 30 giây cuối
    private LocalDateTime scheduledEndTime; // endTime + runtime extension

    public AuctionRoom() {
    }

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

    // ===== GETTER / SETTER CHÍNH =====

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

    // Alias giữ tương thích code cũ
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
        return (itemDescription != null && !itemDescription.isBlank())
                ? itemDescription
                : "Không có mô tả";
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

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    // Alias giữ tương thích giao diện cũ
    public String getNameSeller() {
        return sellerName;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
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

    // Alias tương thích code cũ
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
}