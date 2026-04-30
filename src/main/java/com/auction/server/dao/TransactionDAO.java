package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO đọc lịch sử bid/transaction để phục vụ admin và popup chi tiết sản phẩm.
 */
public class TransactionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDAO.class);

    public List<BidTransaction> getHistoryByRoom(String roomId) {
        // Sắp xếp bid cao nhất trước, cùng giá thì bid sớm hơn đứng trước.
        List<BidTransaction> list = new ArrayList<>();
        String sql = """
                SELECT auction_id, bidder_id, bid_amount, bid_time
                FROM bid_transactions
                WHERE auction_id = ?
                ORDER BY bid_amount DESC, bid_time ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bt = new BidTransaction();
                    bt.setAuctionId(rs.getString("auction_id"));
                    bt.setBidderId(rs.getString("bidder_id"));
                    bt.setBidAmount(rs.getDouble("bid_amount"));

                    // Format thời gian thành chuỗi để TableView client hiển thị trực tiếp.
                    Timestamp timestamp = rs.getTimestamp("bid_time");
                    if (timestamp != null) {
                        bt.setBidTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(timestamp));
                    } else {
                        bt.setBidTime("Unknown");
                    }

                    list.add(bt);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load bid history for room {}.", roomId, e);
        }
        return list;
    }

    public List<Item> getAllItems() {
        // API phụ để đọc danh sách item nếu cần hiển thị kho sản phẩm.
        List<Item> list = new ArrayList<>();
        String sql = """
                SELECT item_id, name, description, current_price
                FROM items
                ORDER BY item_id
                """;

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
            LOGGER.error("Failed to load all items.", e);
        }
        return list;
    }
}
