package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface IAuctionDAO {
    boolean saveAuction(AuctionRoom room, String itemId, String sellerId);

    boolean createAuctionWithItem(AuctionRoom room, Item item, String sellerId);

    String generateNextAuctionId();

    List<AuctionRoom> getAllActiveAuctions();

    List<AuctionRoom> getAllAuctions();

    boolean forceDeleteAuction(String roomId);

    AuctionRoom getAuctionById(String roomId);

    SellerAuctionStats getSellerAuctionStats(String sellerId);

    boolean closeAuctionBySeller(String roomId, String sellerId);

    CloseAuctionResult closeAuctionByTime(String roomId);

    // Map ResultSet từ JOIN auctions/products sang AuctionRoom dùng chung cho client.
    default AuctionRoom mapAuctionRoom(ResultSet rs) throws SQLException {
        AuctionRoom room = new AuctionRoom();

        room.setRoomId(rs.getString("auction_id"));
        room.setItemId(String.valueOf(rs.getInt("product_id")));
        room.setSellerName(rs.getString("seller_id"));
        room.setStatus(rs.getString("status"));
        room.setItemName(rs.getString("product_name"));
        room.setItemDescription(rs.getString("description"));
        room.setCurrentPrice(rs.getDouble("current_price"));
        room.setStartingPrice(rs.getDouble("starting_price"));
        room.setBidStep(rs.getDouble("min_bid_increment"));

        // Cột không tồn tại ở DB nữa, set mặc định để logic phía trên không vỡ
        room.setMinimumJoinAmount(0.0);
        room.setDurationMinutes(0);
        room.setExtensionSeconds(0);

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            room.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("actual_end_time");
        if (endTs != null) {
            room.setEndTime(endTs.toLocalDateTime());
        }

        applySellerStats(room);

        return room;
    }

    // Gắn thống kê seller vào room để client/admin có dữ liệu đánh giá.
    default void applySellerStats(AuctionRoom room) {
        if (room == null || room.getSellerName() == null || room.getSellerName().isBlank()) {
            return;
        }

        SellerAuctionStats stats = getSellerAuctionStats(room.getSellerName());
        room.setSellerReputation(5.0);
        room.setSellerSuccessfulAuctionRate(stats.getSuccessfulAuctionRate());
        room.setSellerAdminCancellationRate(stats.getAdminCancellationRate());
    }

    /**
     * Giá trị kết quả trả về sau khi chốt phiên đấu giá.
     * <p>
     * Vai trò:
     * - Mang trạng thái cuối cùng của phiên sau khi closeAuctionByTime() xử lý.
     * - Cung cấp dữ liệu số dư winner/seller để service broadcast lại cho client.
     * <p>
     * Luồng chính:
     * 1. AuctionDAO tạo instance thông qua factory sold(), unsold() hoặc fail().
     * 2. AuctionRoomService đọc các getter để quyết định message và event cần phát.
     * <p>
     * Business rules:
     * - SOLD chỉ hợp lệ khi có winner và giao dịch chuyển tiền thành công.
     * - UNSOLD là kết quả thành công nhưng không phát sinh winner hoặc thanh toán.
     * <p>
     * Ghi chú kỹ thuật:
     * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
     * - Dependency: Không phụ thuộc DB trực tiếp; là DTO nội bộ của AuctionDAO/AuctionRoomService.
     */
    public static class CloseAuctionResult {
        private final boolean success;
        private final String finalStatus;
        private final String winnerId;
        private final String sellerId;
        private final double finalPrice;
        private final Double winnerBalance;
        private final Double sellerBalance;
        private final String message;

        private CloseAuctionResult(boolean success, String finalStatus, String winnerId, String sellerId,
                                   double finalPrice, Double winnerBalance, Double sellerBalance, String message) {
            this.success = success;
            this.finalStatus = finalStatus;
            this.winnerId = winnerId;
            this.sellerId = sellerId;
            this.finalPrice = finalPrice;
            this.winnerBalance = winnerBalance;
            this.sellerBalance = sellerBalance;
            this.message = message;
        }

        public static CloseAuctionResult sold(String winnerId, String sellerId, double finalPrice,
                                              Double winnerBalance, Double sellerBalance) {
            return new CloseAuctionResult(true, "SOLD", winnerId, sellerId, finalPrice, winnerBalance, sellerBalance,
                    "Auction sold successfully.");
        }

        public static CloseAuctionResult unsold() {
            return new CloseAuctionResult(true, "UNSOLD", null, null, 0.0, null, null,
                    "Auction ended without a buyer.");
        }

        public static CloseAuctionResult fail(String message) {
            return new CloseAuctionResult(false, "ERROR", null, null, 0.0, null, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public String getWinnerId() {
            return winnerId;
        }

        public String getSellerId() {
            return sellerId;
        }

        public double getFinalPrice() {
            return finalPrice;
        }

        public Double getWinnerBalance() {
            return winnerBalance;
        }

        public Double getSellerBalance() {
            return sellerBalance;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Thống kê hiệu quả đấu giá của một seller.
     * <p>
     * Vai trò:
     * - Lưu tổng số phiên, số phiên bán thành công và số phiên bị admin hủy.
     * - Tính tỷ lệ thành công/tỷ lệ bị hủy để đưa vào AuctionRoom hoặc User.
     * <p>
     * Luồng chính:
     * 1. AuctionDAO truy vấn aggregate theo sellerId và tạo SellerAuctionStats.
     * 2. Tầng service/handler đọc tỷ lệ để hiển thị hoặc validate yêu cầu tạo phiên.
     * <p>
     * Business rules:
     * - Số lượng âm được chuẩn hóa về 0 khi khởi tạo.
     * - Nếu seller chưa có phiên nào thì các tỷ lệ trả về 0.0 để tránh chia cho 0.
     * <p>
     * Ghi chú kỹ thuật:
     * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
     * - Dependency: Không phụ thuộc ngoài; được tạo từ dữ liệu aggregate của AuctionDAO.
     */
    public static class SellerAuctionStats {
        private final int totalAuctions;
        private final int soldAuctions;
        private final int adminCanceledAuctions;

        public SellerAuctionStats(int totalAuctions, int soldAuctions, int adminCanceledAuctions) {
            this.totalAuctions = Math.max(totalAuctions, 0);
            this.soldAuctions = Math.max(soldAuctions, 0);
            this.adminCanceledAuctions = Math.max(adminCanceledAuctions, 0);
        }

        public int getTotalAuctions() {
            return totalAuctions;
        }

        public int getSoldAuctions() {
            return soldAuctions;
        }

        public int getAdminCanceledAuctions() {
            return adminCanceledAuctions;
        }

        public double getSuccessfulAuctionRate() {
            if (totalAuctions == 0) return 0.0;
            return (double) soldAuctions / totalAuctions;
        }

        public double getAdminCancellationRate() {
            if (totalAuctions == 0) return 0.0;
            return (double) adminCanceledAuctions / totalAuctions;
        }
    }
}
