package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.server.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public boolean saveAuction(AuctionRoom room, String productId, String sellerId) {
        String initialStatus = "OPEN"; // Đổi SCHEDULED thành OPEN theo DB mới

        int pId = 0;
        try { pId = Integer.parseInt(productId.replaceAll("[^0-9]", "")); }
        catch (Exception e) { pId = (int) (System.currentTimeMillis() % 100000); }

        String sql = "INSERT INTO auctions (auction_id, product_id, start_time, end_time, current_price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomId());
            pstmt.setInt(2, pId);

            // Xử lý an toàn vì DB bắt buộc NOT NULL
            pstmt.setTimestamp(3, room.getStartTime() != null ? Timestamp.valueOf(room.getStartTime()) : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(4, room.getEndTime() != null ? Timestamp.valueOf(room.getEndTime()) : Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)));

            pstmt.setDouble(5, room.getCurrentPrice());
            pstmt.setString(6, initialStatus);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi lưu Auction: " + e.getMessage());
            return false;
        }
    }

    public List<AuctionRoom> getAllActiveAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = "SELECT a.auction_id, p.product_id, a.current_price, a.winner_id, " +
                "a.status, a.start_time, a.end_time " +
                "FROM auctions a " +
                "JOIN products p ON a.product_id = p.product_id " +
                "WHERE a.status IN ('OPEN', 'RUNNING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AuctionRoom room = new AuctionRoom();
                room.setRoomId(rs.getString("auction_id"));
                room.setCurrentPrice(rs.getDouble("current_price"));
                room.setStatus(rs.getString("status"));
                list.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Chốt đơn KHÔNG trừ tiền (vì DB không có cột balance), lưu người thắng vào bảng
    public Object[] closeAuctionAndTransferMoney(String roomId, String sellerId) {
        String getHighestBidSql = "SELECT bidder_id, bid_amount FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC LIMIT 1";
        String updateAuctionStatusSql = "UPDATE auctions SET status = 'FINISHED', winner_id = ? WHERE auction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            String winnerId = null;
            double finalPrice = 0.0;

            // 1. Lấy người ra giá cao nhất từ bảng bids
            try (PreparedStatement pstmt = conn.prepareStatement(getHighestBidSql)) {
                pstmt.setString(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        winnerId = rs.getString("bidder_id");
                        finalPrice = rs.getDouble("bid_amount");
                    }
                }
            }

            // 2. Cập nhật trạng thái thành FINISHED và gán winner_id
            try (PreparedStatement pstmt = conn.prepareStatement(updateAuctionStatusSql)) {
                pstmt.setString(1, winnerId); // Sẽ gán NULL nếu không ai mua (đúng chuẩn DB)
                pstmt.setString(2, roomId);
                pstmt.executeUpdate();
            }

            return new Object[]{true, winnerId, finalPrice, winnerId != null ? "Chốt đơn thành công (Không trừ tiền DB)!" : "Không có ai mua!"};

        } catch (SQLException e) {
            e.printStackTrace();
            return new Object[]{false, null, 0.0, "Lỗi Database!"};
        }
    }

    // LẤY TẤT CẢ PHIÊN ĐẤU GIÁ (CHO ADMIN)
    public List<AuctionRoom> getAllAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = "SELECT a.auction_id, p.product_id, a.current_price, a.status " +
                "FROM auctions a JOIN products p ON a.product_id = p.product_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AuctionRoom room = new AuctionRoom();
                room.setRoomId(rs.getString("auction_id"));
                room.setCurrentPrice(rs.getDouble("current_price"));
                room.setStatus(rs.getString("status")); // Đã mở comment
                list.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ADMIN ÉP HỦY PHIÊN
    public boolean forceDeleteAuction(String roomId) {
        String sql = "UPDATE auctions SET status = 'CANCELED' WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // LẤY CHI TIẾT PHÒNG
    public AuctionRoom getAuctionById(String roomId) {
        // ĐÃ THÊM p.description VÀO SQL
        String sql = "SELECT a.auction_id, p.product_name, p.description, p.seller_id, a.current_price, " +
                "a.status, a.start_time, a.end_time " +
                "FROM auctions a JOIN products p ON a.product_id = p.product_id " +
                "WHERE a.auction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AuctionRoom room = new AuctionRoom();
                    room.setRoomId(rs.getString("auction_id"));
                    room.setItemName(rs.getString("product_name"));
                    room.setItemDescription(rs.getString("description")); // LẤY MÔ TẢ TỪ DB
                    room.setSellerName(rs.getString("seller_id"));
                    room.setCurrentPrice(rs.getDouble("current_price"));
                    room.setStatus(rs.getString("status"));

                    Timestamp startTs = rs.getTimestamp("start_time");
                    if (startTs != null) room.setStartTime(startTs.toLocalDateTime());

                    Timestamp endTs = rs.getTimestamp("end_time");
                    if (endTs != null) room.setEndTime(endTs.toLocalDateTime());

                    return room;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ĐÓNG PHÒNG
    public void closeAuctionByTime(String roomId) {
        // Tận dụng luôn hàm chốt đơn không dùng tiền đã viết trước đó
        closeAuctionAndTransferMoney(roomId, null);
    }
}