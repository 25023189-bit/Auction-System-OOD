package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import java.util.List;

/**
 * Interface for AuctionDAO - defines contract for auction data access operations.
 * Enables dependency injection and mocking in tests.
 */
public interface IAuctionDAO {
    boolean saveAuction(AuctionRoom room, String itemId, String sellerId);
    boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId);
    String generateNextAuctionId();
    List<AuctionRoom> getAllActiveAuctions();
    /**
     * Returns every OPEN/RUNNING auction, including expired ones.
     * The server watcher needs this list so it can finalize auctions that already passed end_time.
     */
    List<AuctionRoom> getAllOpenOrRunningAuctions();
    List<AuctionRoom> getAllAuctions();
    boolean forceDeleteAuction(String roomId);
    AuctionRoom getAuctionById(String roomId);
    SellerAuctionStats getSellerAuctionStats(String sellerId);
    boolean closeAuctionBySeller(String roomId, String sellerId);
    CloseAuctionResult closeAuctionByTime(String roomId);

    /**
     * Inner class for seller auction statistics.
     * Immutable and thread-safe.
     */
    class SellerAuctionStats {
        private final int totalAuctions;
        private final int soldAuctions;
        private final int adminCanceledAuctions;

        public SellerAuctionStats(int totalAuctions, int soldAuctions, int adminCanceledAuctions) {
            this.totalAuctions = Math.max(totalAuctions, 0);
            this.soldAuctions = Math.max(soldAuctions, 0);
            this.adminCanceledAuctions = Math.max(adminCanceledAuctions, 0);
        }

        public int getTotalAuctions() { return totalAuctions; }
        public int getSoldAuctions() { return soldAuctions; }
        public int getAdminCanceledAuctions() { return adminCanceledAuctions; }

        public double getSuccessfulAuctionRate() {
            if (totalAuctions == 0) return 0.0;
            return (double) soldAuctions / totalAuctions;
        }

        public double getAdminCancellationRate() {
            if (totalAuctions == 0) return 0.0;
            return (double) adminCanceledAuctions / totalAuctions;
        }
    }

    /**
     * Inner class for auction closing result.
     * Contains information about whether the auction was successfully closed,
     * and details about winner, seller, and balances if applicable.
     */
    class CloseAuctionResult {
        private final boolean success;
        private final String message;
        private final String winnerId;
        private final String sellerId;
        private final double finalPrice;
        private final Double winnerBalance;
        private final Double sellerBalance;
        private final String finalStatus;

        private CloseAuctionResult(boolean success, String message, String winnerId, String sellerId,
                                   double finalPrice, Double winnerBalance, Double sellerBalance, String finalStatus) {
            this.success = success;
            this.message = message;
            this.winnerId = winnerId;
            this.sellerId = sellerId;
            this.finalPrice = finalPrice;
            this.winnerBalance = winnerBalance;
            this.sellerBalance = sellerBalance;
            this.finalStatus = finalStatus;
        }

        public static CloseAuctionResult sold(String winnerId, String sellerId, double finalPrice,
                                              Double winnerBalance, Double sellerBalance) {
            return new CloseAuctionResult(true, "Auction sold successfully", winnerId, sellerId,
                    finalPrice, winnerBalance, sellerBalance, "SOLD");
        }

        public static CloseAuctionResult unsold() {
            return new CloseAuctionResult(true, "Auction finished with no bids", null, null,
                    0.0, null, null, "UNSOLD");
        }

        public static CloseAuctionResult fail(String message) {
            return new CloseAuctionResult(false, message, null, null, 0.0, null, null, "FAILED");
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getWinnerId() { return winnerId; }
        public String getSellerId() { return sellerId; }
        public double getFinalPrice() { return finalPrice; }
        public Double getWinnerBalance() { return winnerBalance; }
        public Double getSellerBalance() { return sellerBalance; }
        public String getFinalStatus() { return finalStatus; }
    }
}
