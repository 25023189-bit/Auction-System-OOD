package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * Lấy lịch sử giao dịch (đặt giá) của một phòng cụ thể
     */
    public List<BidTransaction> getHistoryByRoom(String roomId) {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY bid_amount DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bt = new BidTransaction();
                    bt.setAuctionId(rs.getString("auction_id"));
                    bt.setBidderId(rs.getString("bidder_id"));
                    bt.setBidAmount(rs.getDouble("bid_amount"));
                    java.sql.Timestamp timestamp = rs.getTimestamp("bid_time");
                    if (timestamp != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        bt.setBidTime(sdf.format(timestamp));
                    } else {
                        bt.setBidTime("Không rõ");
                    }
                    list.add(bt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Admin kiểm tra toàn bộ vật phẩm có trong hệ thống
     */
    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("current_price")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}