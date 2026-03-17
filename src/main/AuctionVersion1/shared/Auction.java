package shared;
import java.io.Serializable;

public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;

    private Item item;
    private double currentHighestBid;
    private String highestBidder; // ID người mua (BD5xxxxx)
    private boolean isCancelled;  // Trạng thái phiên

    public Auction(Item item) {
        this.item = item;
        this.currentHighestBid = item.getStartingPrice();
        this.highestBidder = "Chưa có ai";
        this.isCancelled = false;
    }

    public void updateBid(double amount, String bidderID) {
        this.currentHighestBid = amount;
        this.highestBidder = bidderID;
    }

    public void cancelAuction() { this.isCancelled = true; }

    // Getters
    public Item getItem() { return item; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public String getHighestBidder() { return highestBidder; }
    public boolean isCancelled() { return isCancelled; }
}