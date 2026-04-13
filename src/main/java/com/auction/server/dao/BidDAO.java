package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
import java.sql.*;

public class BidDAO {
    /**
     * Hàm đặt giá: Dùng Transaction để đảm bảo tính toàn vẹn dữ liệu.
     * Cập nhật giá mới lên bảng auctions và ghi vào bid_transactions.
     */
    public boolean placeBid(String auctionId, String bidderId, double bidAmount) {
        // Giá hiện tại giờ nằm ở bảng auctions, dùng FOR UPDATE để tránh Race Condition
        String checkPriceSql = "SELECT current_price FROM auctions WHERE auction_id = ? FOR UPDATE";
        String updatePriceSql = "UPDATE auctions SET current_price = ? WHERE auction_id = ?";
        String insertBidSql = "INSERT INTO bid_transactions (auction_id, bidder_id, bid_amount) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            double currentPrice = 0;
            boolean isAuctionExist = false;

            // 1. Kiểm tra giá hiện tại của phòng đấu giá
            try (PreparedStatement pstmt = conn.prepareStatement(checkPriceSql)) {
                pstmt.setString(1, auctionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentPrice = rs.getDouble("current_price");
                        isAuctionExist = true;
                    }
                }
            }

            // Nếu phòng không tồn tại hoặc giá nhập vào nhỏ hơn/bằng giá hiện tại -> Từ chối
            if (!isAuctionExist || bidAmount <= currentPrice) {
                conn.rollback();
                return false;
            }

            // 2. Cập nhật giá mới trực tiếp lên bảng auctions
            try (PreparedStatement pstmt = conn.prepareStatement(updatePriceSql)) {
                pstmt.setDouble(1, bidAmount);
                pstmt.setString(2, auctionId);
                pstmt.executeUpdate();
            }

            // 3. Ghi lịch sử đặt giá
            try (PreparedStatement pstmt = conn.prepareStatement(insertBidSql)) {
                pstmt.setString(1, auctionId);
                pstmt.setString(2, bidderId);
                pstmt.setDouble(3, bidAmount);
                pstmt.executeUpdate();
            }

            conn.commit(); // Thành công -> Chốt Transaction!
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi Bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}