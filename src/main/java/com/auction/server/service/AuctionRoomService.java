package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;

public class AuctionRoomService {
    private static final long FINAL_WINDOW_SECONDS = 30;

    public Message joinRoom(String roomId, String userId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return new Message("ROOM_FAIL", "SERVER", "Không tìm thấy phòng hoặc phiên đã kết thúc!");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Đổi thành trạng thái FINISHED và CANCELED theo DB mới
        if ("FINISHED".equalsIgnoreCase(room.getStatus())
                || "CANCELED".equalsIgnoreCase(room.getStatus())) {
            return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá này không còn khả dụng!");
        }

        // Đổi SCHEDULED thành OPEN theo DB mới
        if ("OPEN".equalsIgnoreCase(room.getStatus())
                && room.getStartTime() != null
                && now.isBefore(room.getStartTime())) {
            return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá chưa bắt đầu!");
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            java.time.LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);

            if (scheduledEnd == null) {
                return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá thiếu thông tin thời gian!");
            }

            if (!now.isBefore(scheduledEnd)) {
                auctionDAO.closeAuctionByTime(roomId);
                AuctionStateManager.removeState(roomId);
                return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá đã hết giờ và đã được chốt tự động!");
            }

            long remaining = java.time.Duration.between(now, scheduledEnd).getSeconds();

            if (!state.isEntryLocked() && remaining <= FINAL_WINDOW_SECONDS) {
                state.lockEntry(now);
            }

            if (state.isEntryLocked() && !state.hasParticipant(userId)) {
                return new Message("ROOM_FAIL", "SERVER", "Phiên đã khóa người tham gia mới trong 30 giây cuối!");
            }

            state.addParticipant(userId);

            syncRuntimeInfoToRoom(room, state);
            return new Message("ROOM_JOINED", "SERVER", room);
        }
    }

    private java.time.LocalDateTime calculateScheduledEnd(AuctionRoom room, AuctionRuntimeState state) {
        if (room.getEndTime() == null) return null;
        // Lấy thời gian kết thúc gốc cộng thêm số giây gia hạn
        return room.getEndTime().plusSeconds(state.getTotalExtendedSeconds());
    }

    private void syncRuntimeInfoToRoom(AuctionRoom room, AuctionRuntimeState state) {
        room.setExtendedSeconds(state.getTotalExtendedSeconds());
        room.setEntryLocked(state.isEntryLocked());
        room.setScheduledEndTime(calculateScheduledEnd(room, state));
    }

    public Message placeNewBid(String roomId, String userId, double amount) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        // Đổi cách check Role do DB mới không chia class Bidder/Seller
        if (user == null || !"BIDDER".equalsIgnoreCase(user.getRole())) {
            return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy người dùng hoặc bạn không phải người mua!");
        }

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return new Message("BID_FAIL", "SERVER", "Không tìm thấy phòng đấu giá hoặc phiên đã kết thúc!");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if ("OPEN".equalsIgnoreCase(room.getStatus())
                && room.getStartTime() != null
                && now.isBefore(room.getStartTime())) {
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá chưa bắt đầu!");
        }

        if (!"RUNNING".equalsIgnoreCase(room.getStatus()) && !"OPEN".equalsIgnoreCase(room.getStatus())) {
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá không ở trạng thái hợp lệ để đặt giá!");
        }

        // ĐÃ XÓA ĐOẠN CHECK SỐ DƯ (canAfford) VÌ DB KHÔNG CÒN CỘT TIỀN.

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            java.time.LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);

            if (scheduledEnd == null) {
                return new Message("BID_FAIL", "SERVER", "Phiên đấu giá thiếu thông tin thời gian!");
            }

            if (!now.isBefore(scheduledEnd)) {
                auctionDAO.closeAuctionByTime(roomId);
                AuctionStateManager.removeState(roomId);
                return new Message("BID_FAIL", "SERVER", "Phiên đấu giá đã hết giờ và đã được chốt tự động!");
            }

            long remaining = java.time.Duration.between(now, scheduledEnd).getSeconds();

            if (!state.isEntryLocked() && remaining <= FINAL_WINDOW_SECONDS) {
                state.lockEntry(now);
            }

            if (!state.hasParticipant(userId)) {
                return new Message("BID_FAIL", "SERVER", "Bạn không nằm trong danh sách người tham gia hợp lệ của phiên!");
            }

            BidDAO bidDAO = new BidDAO();
            boolean isSuccess = bidDAO.placeBid(roomId, userId, amount);

            if (!isSuccess) {
                return new Message("BID_FAIL", "SERVER", "Giá phải cao hơn mức hiện tại!");
            }

            room.setCurrentPrice(amount);
            room.setHighestBidder(user.getUsername());

            boolean extended = false;
            if (remaining <= FINAL_WINDOW_SECONDS) {
                state.extendBySeconds(room.getExtensionSeconds());
                extended = true;
            }

            syncRuntimeInfoToRoom(room, state);

            String action = extended ? "BID_SUCCESS_EXTENDED" : "BID_SUCCESS";
            return new Message(action, user.getUsername(), room);
        }
    }

    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        return true;
    }
}