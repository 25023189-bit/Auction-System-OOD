package common.models.Auctions;

import java.io.Serializable;

public class AuctionRoom implements Serializable {
    private String roomId;
    private String itemName;
    private double currentPrice;
    private String highestBidder;

    public AuctionRoom(String roomId, String itemName, double startingPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = startingPrice;
        this.highestBidder = "Chưa có ai";
    }

    // --- CÁC HÀM MÀ CLIENT HANDLER ĐANG TÌM ---
    public String getRoomId() {
        return roomId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    // Logic đặt giá tạm thời (Mock)
    public boolean placeNewBid(String bidderId, double amount) {
        if (amount > this.currentPrice) {
            this.currentPrice = amount;
            this.highestBidder = bidderId;
            return true;
        }
        return false;
    }
}