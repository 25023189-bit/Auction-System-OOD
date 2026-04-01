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

    /**
     * Constructor khởi tạo phòng mới.
     */
    public AuctionRoom(String roomId, String itemName, double startingPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = startingPrice;
        this.highestBidder = "None"; // Chuẩn hóa giá trị khi chưa có ai đặt
    }

    // --- GETTERS (Dùng cho ClientHandler, MockDB, AuctionController) ---
    public String getRoomId() { return roomId; }
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }

    // --- SETTERS (Dùng khi cần cập nhật thông tin phòng) ---
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setHighestBidder(String highestBidder) { this.highestBidder = highestBidder; }

    /**
     * Logic nghiệp vụ: Kiểm tra và đặt giá mới.
     * Trả về true nếu giá thầu hợp lệ và được chấp nhận.
     */
    public synchronized boolean placeNewBid(String bidderId, double amount) {
        // Giá mới phải cao hơn giá hiện tại ít nhất 1 đơn vị (hoặc tùy bạn chỉnh)
        if (amount > this.currentPrice) {
            this.currentPrice = amount;
            this.highestBidder = bidderId;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Room[" + roomId + " - " + itemName + " - Price: " + currentPrice + "]";
    }
}