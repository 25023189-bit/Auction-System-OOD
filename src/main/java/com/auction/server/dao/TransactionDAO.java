package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
import com.auction.common.model.Item;

public class TransactionDAO {
    // Xem lịch sử đặt giá của một phòng cụ thể
    public java.util.List<BidTransaction> getHistoryByRoom(String roomId) {
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY bid_time DESC";
        // ...
    }

    // Admin kiểm tra kho vật phẩm của toàn Server
    public java.util.List<Item> getAllItems() {
        String sql = "SELECT * FROM items";
        // ...
    }
}
