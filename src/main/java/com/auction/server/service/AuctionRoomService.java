package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Bidder;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;

public class AuctionRoomService {
    private static final long FINAL_WINDOW_SECONDS = 30;
    private static final long EXTENSION_SECONDS = 60;

    public Message joinRoom(String roomId, String userId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return new Message("ROOM_FAIL", "SERVER", "Không tìm thấy phòng hoặc phiên đã kết thúc!");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if ("SOLD".equalsIgnoreCase(room.getStatus())
                || "UNSOLD".equalsIgnoreCase(room.getStatus())
                || "CANCELED_BY_ADMIN".equalsIgnoreCase(room.getStatus())) {
            return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá này không còn khả dụng!");
        }

        if ("SCHEDULED".equalsIgnoreCase(room.getStatus())
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
        if (room.getStartTime() == null) return null;
        return room.getStartTime()
                .plusMinutes(room.getDurationMinutes())
                .plusSeconds(state.getTotalExtendedSeconds());
    }

    private void syncRuntimeInfoToRoom(AuctionRoom room, AuctionRuntimeState state) {
        room.setExtendedSeconds(state.getTotalExtendedSeconds());
        room.setEntryLocked(state.isEntryLocked());
        room.setScheduledEndTime(calculateScheduledEnd(room, state));
    }

    public Message placeNewBid(String roomId, String userId, double amount) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        if (!(user instanceof Bidder)) {
            return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy người dùng hoặc bạn không phải người mua!");
        }

        Bidder bidder = (Bidder) user;

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return new Message("BID_FAIL", "SERVER", "Không tìm thấy phòng đấu giá hoặc phiên đã kết thúc!");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if ("SCHEDULED".equalsIgnoreCase(room.getStatus())
                && room.getStartTime() != null
                && now.isBefore(room.getStartTime())) {
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá chưa bắt đầu!");
        }

        if (!"RUNNING".equalsIgnoreCase(room.getStatus()) && !"SCHEDULED".equalsIgnoreCase(room.getStatus())) {
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá không ở trạng thái hợp lệ để đặt giá!");
        }

        if (!bidder.canAfford(amount)) {
            return new Message("BID_FAIL", "SERVER", "Số dư ví không đủ " + amount + "$ để đặt giá!");
        }

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
            room.setHighestBidder(bidder.getUsername());

            boolean extended = false;
            if (remaining <= FINAL_WINDOW_SECONDS) {
                state.extendBySeconds(EXTENSION_SECONDS);
                extended = true;
            }

            syncRuntimeInfoToRoom(room, state);

            String action = extended ? "BID_SUCCESS_EXTENDED" : "BID_SUCCESS";
            return new Message(action, bidder.getUsername(), room);
        }
    }

    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        boolean result = true;
        System.out.println("Chức năng đang bảo trì");
        return result;
    }
}