package server.database;

import common.models.Auctions.BidTransaction;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class BidDAO {

    // Hàm lưu một lượt đặt giá mới vào MySQL
    public boolean saveBid(BidTransaction bid) {
        String sql = "INSERT INTO bid_transactions (auction_id, bidder_id, bid_amount, bid_rank, is_highest) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 1. auction_id (Mã phòng đấu giá)
            pstmt.setString(1, bid.getAuctionId());

            // 2. bidder_id (Mã người đặt - chính là customer_id của User)
            pstmt.setString(2, bid.getBidderId());

            // 3. bid_amount (Số tiền đặt)
            pstmt.setDouble(3, bid.getBidAmount());

            // 4. bid_rank (Thứ tự lượt đặt - bạn có thể tạm để là 1 hoặc xử lý logic tăng dần sau)
            pstmt.setInt(4, bid.getBidRank());

            // 5. is_highest (Có phải giá cao nhất hiện tại không?)
            pstmt.setBoolean(5, true);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            System.out.println("Lỗi lưu giao dịch đấu giá: " + e.getMessage());
            return false;
        }
    }
}