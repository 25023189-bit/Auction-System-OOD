package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO implements IAuctionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionDAO.class);

    @Override
    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        String sql = """
                INSERT INTO auctions (auction_id, product_id, seller_id, start_time, end_time, min_bid_increment, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomId());
            stmt.setString(2, itemId);
            stmt.setString(3, sellerId);
            stmt.setTimestamp(4, Timestamp.valueOf(room.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(room.getEndTime()));
            stmt.setDouble(6, room.getMinimumJoinAmount()); // hoặc min_bid_increment tùy thuộc thuộc tính của bạn
            stmt.setString(7, room.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi lưu phòng đấu giá: {}", room.getRoomId(), e);
            return false;
        }
    }

    @Override
    public boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId) {
        // Thực hiện lưu cả item và đấu giá (có thể gọi thông qua kết nối transaction)
        return saveAuction(room, item.getId(), sellerId);
    }

    @Override
    public String generateNextAuctionId() {
        String sql = "SELECT auction_id FROM auctions ORDER BY auction_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("auction_id");
                if (lastId != null && lastId.startsWith("A")) {
                    int num = Integer.parseInt(lastId.substring(1));
                    return String.format("A%03d", num + 1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi sinh mã đấu giá tiếp theo", e);
        }
        return "A001";
    }

    @Override
    public List<AuctionRoom> getAllActiveAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE status = 'RUNNING' OR status = 'OPEN'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAuctionRoom(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi lấy danh sách đấu giá đang hoạt động", e);
        }
        return list;
    }

    @Override
    public List<AuctionRoom> getAllAuctions() {
        List<AuctionRoom> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAuctionRoom(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi lấy toàn bộ danh sách đấu giá", e);
        }
        return list;
    }

    @Override
    public boolean forceDeleteAuction(String roomId) {
        String sql = "DELETE FROM auctions WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi xóa phòng đấu giá: {}", roomId, e);
            return false;
        }
    }

    @Override
    public AuctionRoom getAuctionById(String roomId) {
        String sql = "SELECT * FROM auctions WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionRoom(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi lấy thông tin phòng bằng ID: {}", roomId, e);
        }
        return null;
    }

    @Override
    public SellerAuctionStats getSellerAuctionStats(String sellerId) {
        // Trả về dữ liệu trống hoặc tính toán tùy logic bài của bạn
        return new SellerAuctionStats(0, 0, 0);
    }

    @Override
    public boolean closeAuctionBySeller(String roomId, String sellerId) {
        String sql = "UPDATE auctions SET status = 'FINISHED' WHERE auction_id = ? AND seller_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            stmt.setString(2, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Lỗi khi seller chủ động đóng phòng: {}", roomId, e);
            return false;
        }
    }

    @Override
    public CloseAuctionResult closeAuctionByTime(String roomId) {
        // Đây chính là hàm đang bị báo thiếu khiến hệ thống bị lỗi!
        String sql = "UPDATE auctions SET status = 'FINISHED' WHERE auction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            if (stmt.executeUpdate() > 0) {
                return CloseAuctionResult.unsold();
            }
        } catch (SQLException e) {
            LOGGER.error("Lỗi hệ thống tự động đóng phòng hết giờ: {}", roomId, e);
        }
        return CloseAuctionResult.fail("Database error during automatic closure.");
    }

    private AuctionRoom mapAuctionRoom(ResultSet rs) throws SQLException {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId(rs.getString("auction_id"));
        room.setStatus(rs.getString("status"));
        if (rs.getTimestamp("start_time") != null) {
            room.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            room.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }
        room.setMinimumJoinAmount(rs.getDouble("min_bid_increment"));
        return room;
    }
}