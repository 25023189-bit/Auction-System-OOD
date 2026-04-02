package common.models.Auctions;

import java.io.Serializable;

public class AuctionRoom implements Serializable {
    private String roomId;
    private String itemName;
    private double currentPrice;
    private String highestBidder;
    private String nameSeller;
    private String highestBidderId = "Chưa có ai"; // Bắt buộc lưu ID (VD: BD001)

    public AuctionRoom(String roomId, String itemName, double startingPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = startingPrice;
        this.highestBidder = "Chưa có ai";
    }

    public AuctionRoom(String roomId, String itemName, double startingPrice, String nameSeller) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = startingPrice;
        this.nameSeller = nameSeller;
    }

    // Cập nhật giá khi có người trả cao hơn
    public synchronized boolean placeNewBid(String userId, double bidAmount) {
        if (bidAmount > this.currentPrice) {
            this.currentPrice = bidAmount;
            this.highestBidderId = userId; // Treo ID người giá cao nhất
            return true;
        }
        return false;
    }

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


}