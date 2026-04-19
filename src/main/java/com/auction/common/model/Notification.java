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

    // Getters/Setters...
}