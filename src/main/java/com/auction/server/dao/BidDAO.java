package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BidDAO {

    public BidResult placeBid(String auctionId, String bidderId, double bidAmount) {
        String selectAuctionSql = """
                SELECT a.item_id, i.current_price, u.balance, a.bid_step
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
                JOIN users u ON u.customer_id = ?
                WHERE a.auction_id = ?
                FOR UPDATE
                """;

        String clearHighestSql = """
                UPDATE bid_transactions
                SET is_highest = 0
                WHERE auction_id = ? AND is_highest = 1
                """;

        String insertBidSql = """
                INSERT INTO bid_transactions
                (auction_id, bidder_id, bid_amount, bid_rank, is_highest, bid_time)
                VALUES (?, ?, ?, ?, ?, NOW())
                """;

        String updateItemPriceSql = """
                UPDATE items
                SET current_price = ?
                WHERE item_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String itemId = null;
            double currentPrice = 0;
            double currentBalance = 0;
            double bidStep = 0;
            boolean auctionExists = false;

            try (PreparedStatement pstmt = conn.prepareStatement(selectAuctionSql)) {
                pstmt.setString(1, bidderId);
                pstmt.setString(2, auctionId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        auctionExists = true;
                        itemId = rs.getString("item_id");
                        currentPrice = rs.getDouble("current_price");
                        currentBalance = rs.getDouble("balance");
                        bidStep = rs.getDouble("bid_step");
                    }
                }
            }

            if (!auctionExists || itemId == null) {
                conn.rollback();
                return BidResult.fail(BidStatus.AUCTION_NOT_FOUND, "Auction not found.");
            }

            double minimumAllowedBid = currentPrice + bidStep;
            if (bidAmount < minimumAllowedBid) {
                conn.rollback();
                return BidResult.fail(BidStatus.BID_TOO_LOW, "Bid must increase by at least " + bidStep + " from the current price.");
            }

            if (bidAmount > currentBalance) {
                conn.rollback();
                return BidResult.fail(BidStatus.INSUFFICIENT_BALANCE, "Current balance is not enough for this bid amount.");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(clearHighestSql)) {
                pstmt.setString(1, auctionId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertBidSql)) {
                pstmt.setString(1, auctionId);
                pstmt.setString(2, bidderId);
                pstmt.setDouble(3, bidAmount);
                pstmt.setInt(4, 1);
                pstmt.setInt(5, 1);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(updateItemPriceSql)) {
                pstmt.setDouble(1, bidAmount);
                pstmt.setString(2, itemId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return BidResult.success();
        } catch (SQLException e) {
            System.err.println("Bid error: " + e.getMessage());
            e.printStackTrace();
            return BidResult.fail(BidStatus.ERROR, "Database error while placing bid.");
        }
    }

    public enum BidStatus {
        SUCCESS,
        AUCTION_NOT_FOUND,
        BID_TOO_LOW,
        INSUFFICIENT_BALANCE,
        ERROR
    }

    public static class BidResult {
        private final boolean success;
        private final BidStatus status;
        private final String message;

        private BidResult(boolean success, BidStatus status, String message) {
            this.success = success;
            this.status = status;
            this.message = message;
        }

        public static BidResult success() {
            return new BidResult(true, BidStatus.SUCCESS, null);
        }

        public static BidResult fail(BidStatus status, String message) {
            return new BidResult(false, status, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public BidStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

