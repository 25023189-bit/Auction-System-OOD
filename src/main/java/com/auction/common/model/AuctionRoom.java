package com.auction.common.model;

import java.io.Serializable;

/**
 * Model đại diện cho một Phòng đấu giá.
 * Chứa thông tin vật phẩm và logic kiểm tra giá thầu (Bid).
 */
public class AuctionRoom implements Serializable {
    private static final long serialVersionUID = 1L; // Đảm bảo truyền nhận object qua mạng ổn định

    private String roomId;
    private String itemName;
    private double currentPrice;
    private String highestBidder;
    private String nameSeller;
    private String highestBidderId = "Chưa có ai";
    private String status;

    /**
     * Constructor khởi tạo phòng mới.
     */
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

    // Hàm cập nhật biến treo
    public synchronized boolean placeNewBid(String userId, double bidAmount) {
        if (bidAmount > this.currentPrice) {
            this.currentPrice = bidAmount;
            this.highestBidderId = userId; // Ghi nhớ ID người giá cao nhất
            return true;
        }
        return false;
    }

    // --- GETTERS (Dùng cho ClientHandler, MockDB, AuctionController) ---
    public String getRoomId() { return roomId; }
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public String getHighestBidderId() {
        return this.highestBidderId;
    }
    public String getNameSeller() {
        return nameSeller;
    }
    public String getStatus() {
        return status;
    }

    // --- SETTERS (Dùng khi cần cập nhật thông tin phòng) ---
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Room[" + roomId + " - " + itemName + " - Price: " + currentPrice + "]";
    }
}