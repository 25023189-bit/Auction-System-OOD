package com.auction.common.model;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private String userId; // Người nhận
    private String auctionId; // Phiên đấu giá liên quan
    private String message;
    private NotificationType type;
    private boolean isRead = false;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private LocalDateTime createdAt;

    public Notification(String userId, String auctionId, String message, NotificationType type) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // --- FULL GETTERS VÀ SETTERS ---

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}