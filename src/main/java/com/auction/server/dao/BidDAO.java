package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;

import java.sql.*;

public class BidDAO {

    public boolean placeBid(String auctionId, String bidderId, double bidAmount) {
        String selectAuctionSql = """
                SELECT a.item_id, i.current_price
                FROM auctions a
                JOIN items i ON a.item_id = i.item_id
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
            boolean auctionExists = false;

            try (PreparedStatement pstmt = conn.prepareStatement(selectAuctionSql)) {
                pstmt.setString(1, auctionId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        auctionExists = true;
                        itemId = rs.getString("item_id");
                        currentPrice = rs.getDouble("current_price");
                    }
                }
            }

            if (!auctionExists || itemId == null || bidAmount <= currentPrice) {
                conn.rollback();
                return false;
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
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi Bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}