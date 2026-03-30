package common.models.Auctions;

import java.io.Serializable;

public class AuctionRoom implements Serializable {
    private String roomId;
    private String itemName;
    private double currentPrice;
    private String highestBidder;
    private String nameSeller;

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
        this.highestBidder = "Chưa có ai";
        this.nameSeller = nameSeller;
    }

    // Cập nhật giá khi có người trả cao hơn
    public boolean placeNewBid(String bidder, double amount) {
        if (amount > this.currentPrice) {
            this.currentPrice = amount;
            this.highestBidder = bidder;
            return true; // Trả giá thành công
        }
        return false; // Trả giá thất bại
    }

    public String getRoomId() { return roomId; }
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
}