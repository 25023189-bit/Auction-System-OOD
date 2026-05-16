package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
import java.util.List;

/**
 * Interface for BidDAO - defines contract for bid data access operations.
 * Enables dependency injection and mocking in tests.
 */
public interface IBidDAO {
    BidResult placeBid(String auctionId, String bidderId, double amount);
    List<BidTransaction> getHistoryByRoom(String roomId);

    /**
     * Inner class for bid result.
     * Contains information about whether the bid was successfully placed.
     */
    class BidResult {
        private final boolean success;
        private final String message;

        public BidResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static BidResult success() {
            return new BidResult(true, null);
        }

        public static BidResult fail(String message) {
            return new BidResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
