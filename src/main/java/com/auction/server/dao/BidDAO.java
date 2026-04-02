package com.auction.server.dao;

import com.auction.server.utils.DatabaseConnection;
// SỬA: Import đúng địa chỉ package thực tế của dự án
import com.auction.common.model.BidTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BidDAO {

    /**
     * Hàm lưu một lượt đặt giá mới vào MySQL.
     * Chú ý: Đảm bảo class BidTransaction đã có các hàm Getter tương ứng.
     */
    public boolean saveBid(BidTransaction bid) {
        String sql = "INSERT INTO bid_transactions (auction_id, bidder_id, bid_amount, bid_rank, is_highest) " +
                "VALUES (?, ?, ?, ?, ?)";

        // Sử dụng Try-with-resources để tự động đóng Connection và PreparedStatement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("❌ Không thể kết nối Database để lưu Bid!");
                return false;
            }

            // 1. auction_id (Mã phòng đấu giá)
            pstmt.setString(1, bid.getAuctionId());

            // 2. bidder_id (Mã người đặt)
            pstmt.setString(2, bid.getBidderId());

            // 3. bid_amount (Số tiền đặt)
            pstmt.setDouble(3, bid.getBidAmount());

            // 4. bid_rank (Thứ tự lượt đặt)
            pstmt.setInt(4, bid.getBidRank());

            // 5. is_highest (Mặc định là true khi vừa đặt giá cao nhất mới)
            pstmt.setBoolean(5, true);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi SQL khi lưu giao dịch đấu giá: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}