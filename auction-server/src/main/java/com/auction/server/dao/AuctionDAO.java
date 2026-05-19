package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO truy cập dữ liệu phiên đấu giá.
 *
 * Ghi nhớ khi vấn đáp:
 * - Bảng auctions không có seller_id; seller được lưu ở cột created_by.
 * - Thông tin sản phẩm nằm ở bảng products, nên các query trả AuctionRoom phải JOIN products.
 * - BidDAO kiểm tra số dư ở users.balance, vì vậy chốt phiên cũng cộng/trừ đúng cột này.
 */
public class AuctionDAO implements IAuctionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionDAO.class);

    private static final String ROOM_SELECT = """
            SELECT a.auction_id, a.product_id, a.created_by AS seller_id, a.status,
                   a.start_time, a.end_time, a.actual_end_time, a.min_bid_increment,
                   a.auto_extension_minutes,
                   p.product_name, p.description, p.current_price, p.starting_price
            FROM auctions a
            JOIN products p ON a.product_id = p.product_id
            """;

    @Override
    public boolean saveAuction(AuctionRoom room, String itemId, String sellerId) {
        if (room == null || room.getStartTime() == null || room.getEndTime() == null) {
            LOGGER.warn("Cannot save auction because required schedule information is missing.");
            return false;
        }

        int productId;
        try {
            productId = Integer.parseInt(itemId);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid product_id for auction {}: {}", room.getRoomId(), itemId);
            return false;
        }

        String sql = """
                INSERT INTO auctions (
                    auction_id, product_id, created_by, start_time, end_time,
                    actual_end_time, min_bid_increment, auto_extension_minutes, status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindAuctionInsert(stmt, room, productId, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to save auction {}.", room.getRoomId(), e);
            return false;
        }
    }

    @Override
    public boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId) {
        if (room == null || item == null || room.getStartTime() == null || room.getEndTime() == null) {
            LOGGER.warn("Cannot create auction because room/item data is incomplete.");
            return false;
        }

        String insertProductSql = """
                INSERT INTO products (
                    seller_id, product_type, product_name, description,
                    starting_price, current_price
                )
                VALUES (?, 'ELECTRONICS', ?, ?, ?, ?)
                """;

        String insertAuctionSql = """
                INSERT INTO auctions (
                    auction_id, product_id, created_by, start_time, end_time,
                    actual_end_time, min_bid_increment, auto_extension_minutes, status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int productId;
                try (PreparedStatement stmt = conn.prepareStatement(insertProductSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, sellerId);
                    stmt.setString(2, item.getProductName());
                    stmt.setString(3, item.getDescription());
                    stmt.setDouble(4, item.getStartingPrice());
                    stmt.setDouble(5, item.getStartingPrice());
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            LOGGER.error("Product insert succeeded but no generated product_id was returned.");
                            return false;
                        }
                        productId = keys.getInt(1);
                    }
                }

                room.setItemId(String.valueOf(productId));
                try (PreparedStatement stmt = conn.prepareStatement(insertAuctionSql)) {
                    bindAuctionInsert(stmt, room, productId, sellerId);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                rollbackQuietly(conn);
                LOGGER.error("Failed to create auction with item. roomId={}, sellerId={}",
                        room.getRoomId(), sellerId, e);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to open database connection for auction creation.", e);
            return false;
        }
    }

    @Override
    public String generateNextAuctionId() {
        String sql = """
                SELECT auction_id
                FROM auctions
                WHERE auction_id REGEXP '^AU1[0-9]{5}$'
                ORDER BY auction_id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("auction_id");
                int number = Integer.parseInt(lastId.substring(3));
                return String.format("AU1%05d", number + 1);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to generate next auction id.", e);
        }
        return "AU100001";
    }

    @Override
    public List<AuctionRoom> getAllActiveAuctions() {
        // Lobby chỉ hiển thị phiên còn mở/chạy và chưa quá thời điểm kết thúc trong DB.
        String sql = ROOM_SELECT + """
                WHERE a.status IN ('OPEN', 'RUNNING')
                  AND COALESCE(a.actual_end_time, a.end_time) > NOW(3)
                ORDER BY a.start_time ASC
                """;
        return loadRooms(sql);
    }

    @Override
    public List<AuctionRoom> getAllOpenOrRunningAuctions() {
        // Watcher cần thấy cả phiên quá giờ để gọi service chốt trạng thái.
        String sql = ROOM_SELECT + """
                WHERE a.status IN ('OPEN', 'RUNNING')
                ORDER BY a.start_time ASC
                """;
        return loadRooms(sql);
    }

    @Override
    public List<AuctionRoom> getAllAuctions() {
        String sql = ROOM_SELECT + "ORDER BY a.start_time DESC";
        return loadRooms(sql);
    }

    @Override
    public boolean forceDeleteAuction(String roomId) {
        // Admin hủy phiên bằng status để còn lịch sử, không xóa cứng dữ liệu khỏi DB.
        String sql = """
                UPDATE auctions
                SET status = 'CANCELED', actual_end_time = NOW(3)
                WHERE auction_id = ? AND status IN ('OPEN', 'RUNNING')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to cancel auction {} by admin.", roomId, e);
            return false;
        }
    }

    @Override
    public AuctionRoom getAuctionById(String roomId) {
        String sql = ROOM_SELECT + "WHERE a.auction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAuctionRoom(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load auction {}.", roomId, e);
        }
        return null;
    }

    @Override
    public SellerAuctionStats getSellerAuctionStats(String sellerId) {
        String sql = """
                SELECT COUNT(*) AS total_auctions,
                       SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) AS sold_auctions,
                       SUM(CASE WHEN status = 'CANCELED' THEN 1 ELSE 0 END) AS canceled_auctions
                FROM auctions
                WHERE created_by = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new SellerAuctionStats(
                            rs.getInt("total_auctions"),
                            rs.getInt("sold_auctions"),
                            rs.getInt("canceled_auctions")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load seller auction stats for {}.", sellerId, e);
        }
        return new SellerAuctionStats(0, 0, 0);
    }

    @Override
    public boolean closeAuctionBySeller(String roomId, String sellerId) {
        String sql = """
                UPDATE auctions
                SET status = 'CANCELED', actual_end_time = NOW(3)
                WHERE auction_id = ? AND created_by = ? AND status IN ('OPEN', 'RUNNING')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
            stmt.setString(2, sellerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to close auction {} by seller {}.", roomId, sellerId, e);
            return false;
        }
    }

    @Override
    public CloseAuctionResult closeAuctionByTime(String roomId) {
        String auctionSql = """
                SELECT auction_id, product_id, created_by AS seller_id, status
                FROM auctions
                WHERE auction_id = ?
                FOR UPDATE
                """;

        String highestBidSql = """
                SELECT bidder_id, bid_amount
                FROM bid_transactions
                WHERE auction_id = ? AND is_highest = 1
                ORDER BY bid_amount DESC, bid_time ASC
                LIMIT 1
                """;

        String updateAuctionSql = """
                UPDATE auctions
                SET status = ?, winner_id = ?, final_price = ?, actual_end_time = NOW(3)
                WHERE auction_id = ?
                """;

        String updateProductSql = """
                UPDATE products
                SET status = ?, current_price = ?
                WHERE product_id = ?
                """;

        String debitWinnerSql = """
                UPDATE users
                SET balance = balance - ?
                WHERE customer_id = ? AND balance >= ?
                """;

        String creditSellerSql = """
                UPDATE users
                SET balance = balance + ?
                WHERE customer_id = ?
                """;

        String selectBalancesSql = """
                SELECT customer_id, balance
                FROM users
                WHERE customer_id IN (?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int productId;
                String sellerId;
                String currentStatus;

                try (PreparedStatement stmt = conn.prepareStatement(auctionSql)) {
                    stmt.setString(1, roomId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return CloseAuctionResult.fail("Auction not found.");
                        }
                        productId = rs.getInt("product_id");
                        sellerId = rs.getString("seller_id");
                        currentStatus = rs.getString("status");
                    }
                }

                if (!isCloseableStatus(currentStatus)) {
                    conn.rollback();
                    return CloseAuctionResult.fail("Auction is already in a finished state.");
                }

                String winnerId = null;
                double finalPrice = 0.0;
                try (PreparedStatement stmt = conn.prepareStatement(highestBidSql)) {
                    stmt.setString(1, roomId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            winnerId = rs.getString("bidder_id");
                            finalPrice = rs.getDouble("bid_amount");
                        }
                    }
                }

                if (winnerId == null) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateAuctionSql)) {
                        stmt.setString(1, "FINISHED");
                        stmt.setNull(2, Types.VARCHAR);
                        stmt.setNull(3, Types.DECIMAL);
                        stmt.setString(4, roomId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    return CloseAuctionResult.unsold();
                }

                try (PreparedStatement stmt = conn.prepareStatement(debitWinnerSql)) {
                    stmt.setDouble(1, finalPrice);
                    stmt.setString(2, winnerId);
                    stmt.setDouble(3, finalPrice);
                    if (stmt.executeUpdate() == 0) {
                        conn.rollback();
                        return CloseAuctionResult.fail("Winner balance is not enough to settle this auction.");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(creditSellerSql)) {
                    stmt.setDouble(1, finalPrice);
                    stmt.setString(2, sellerId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateAuctionSql)) {
                    stmt.setString(1, "PAID");
                    stmt.setString(2, winnerId);
                    stmt.setDouble(3, finalPrice);
                    stmt.setString(4, roomId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateProductSql)) {
                    stmt.setString(1, "SOLD");
                    stmt.setDouble(2, finalPrice);
                    stmt.setInt(3, productId);
                    stmt.executeUpdate();
                }

                Map<String, Double> balances = new HashMap<>();
                try (PreparedStatement stmt = conn.prepareStatement(selectBalancesSql)) {
                    stmt.setString(1, winnerId);
                    stmt.setString(2, sellerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            balances.put(rs.getString("customer_id"), rs.getDouble("balance"));
                        }
                    }
                }

                conn.commit();
                return CloseAuctionResult.sold(
                        winnerId,
                        sellerId,
                        finalPrice,
                        balances.get(winnerId),
                        balances.get(sellerId)
                );
            } catch (SQLException e) {
                rollbackQuietly(conn);
                LOGGER.error("Failed to close expired auction {}.", roomId, e);
                return CloseAuctionResult.fail("Database error during automatic closure.");
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to open database connection when closing auction {}.", roomId, e);
            return CloseAuctionResult.fail("Database connection error during automatic closure.");
        }
    }

    private List<AuctionRoom> loadRooms(String sql) {
        List<AuctionRoom> rooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapAuctionRoom(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load auction rooms.", e);
        }
        return rooms;
    }

    private void bindAuctionInsert(PreparedStatement stmt, AuctionRoom room, int productId, String sellerId)
            throws SQLException {
        LocalDateTime actualEndTime = room.getActualEndTime() != null
                ? room.getActualEndTime()
                : room.getEndTime();

        stmt.setString(1, room.getRoomId());
        stmt.setInt(2, productId);
        stmt.setString(3, sellerId);
        stmt.setTimestamp(4, Timestamp.valueOf(room.getStartTime()));
        stmt.setTimestamp(5, Timestamp.valueOf(room.getEndTime()));
        stmt.setTimestamp(6, Timestamp.valueOf(actualEndTime));
        stmt.setDouble(7, resolveBidStep(room));
        stmt.setInt(8, Math.max(0, (int) Math.ceil(room.getExtensionSeconds() / 60.0)));
        stmt.setString(9, normalizeAuctionStatus(room.getStatus(), room.getStartTime()));
    }

    private AuctionRoom mapAuctionRoom(ResultSet rs) throws SQLException {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId(rs.getString("auction_id"));
        room.setItemId(String.valueOf(rs.getInt("product_id")));
        room.setSellerName(rs.getString("seller_id"));
        room.setStatus(rs.getString("status"));
        room.setItemName(rs.getString("product_name"));
        room.setItemDescription(rs.getString("description"));
        room.setStartingPrice(rs.getDouble("starting_price"));
        room.setCurrentPrice(rs.getDouble("current_price"));
        room.setBidStep(rs.getDouble("min_bid_increment"));

        // Schema hiện tại chưa có cột minimum_join_amount, nên chỉ validate số dư ở bước đặt giá.
        room.setMinimumJoinAmount(0.0);
        room.setExtensionSeconds(rs.getInt("auto_extension_minutes") * 60);

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            room.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("actual_end_time");
        if (endTs == null) {
            endTs = rs.getTimestamp("end_time");
        }
        if (endTs != null) {
            room.setEndTime(endTs.toLocalDateTime());
        }

        return room;
    }

    private boolean isCloseableStatus(String status) {
        return "OPEN".equalsIgnoreCase(status) || "RUNNING".equalsIgnoreCase(status);
    }

    private String normalizeAuctionStatus(String status, LocalDateTime startTime) {
        if ("OPEN".equalsIgnoreCase(status) || "RUNNING".equalsIgnoreCase(status)) {
            return status.toUpperCase();
        }
        return startTime != null && startTime.isAfter(LocalDateTime.now()) ? "OPEN" : "RUNNING";
    }

    private double resolveBidStep(AuctionRoom room) {
        if (room.getBidStep() > 0) {
            return room.getBidStep();
        }
        if (room.getMinimumJoinAmount() > 0) {
            return room.getMinimumJoinAmount();
        }
        return 1.0;
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            LOGGER.warn("Rollback failed.", rollbackError);
        }
    }
}
