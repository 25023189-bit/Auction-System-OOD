package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.main.AuctionServer;
import com.auction.server.dao.AuctionDAO.CloseAuctionResult;

import java.time.Duration;
import java.time.LocalDateTime;

public class AuctionRoomService {
    private static final long FINAL_WINDOW_SECONDS = 30L;

    public Message joinRoom(String roomId, String userId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return fail("ROOM_FAIL", "Không tìm thấy phòng hoặc phiên đã kết thúc!");
        }

        LocalDateTime now = LocalDateTime.now();

        Message preCheck = validateJoinByStatus(room, now);
        if (preCheck != null) {
            return preCheck;
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);
            if (scheduledEnd == null) {
                return fail("ROOM_FAIL", "Phiên đấu giá thiếu thông tin thời gian!");
            }

            if (!now.isBefore(scheduledEnd)) {
                return handleExpiredAuction(roomId, "ROOM_FAIL");
            }

            long remainingSeconds = Duration.between(now, scheduledEnd).getSeconds();
            applyFinalWindowRules(state, now, remainingSeconds);

            if (state.isEntryLocked() && !state.hasParticipant(userId)) {
                return fail("ROOM_FAIL", "Phiên đã khóa người tham gia mới trong 30 giây cuối!");
            }

            state.addParticipant(userId);
            syncRuntimeInfoToRoom(room, state);

            return new Message("ROOM_JOINED", "SERVER", room);
        }
    }

    public Message placeNewBid(String roomId, String userId, double amount) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        if (user == null) {
            return fail("BID_FAIL", "Không tìm thấy người dùng!");
        }

        if (!"BIDDER".equalsIgnoreCase(user.getRole())) {
            return fail("BID_FAIL", "Bạn không phải người mua để tham gia đặt giá!");
        }

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return fail("BID_FAIL", "Không tìm thấy phòng đấu giá hoặc phiên đã kết thúc!");
        }

        LocalDateTime now = LocalDateTime.now();

        Message preCheck = validateBidByStatus(room, now);
        if (preCheck != null) {
            return preCheck;
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);
            if (scheduledEnd == null) {
                return fail("BID_FAIL", "Phiên đấu giá thiếu thông tin thời gian!");
            }

            if (!now.isBefore(scheduledEnd)) {
                return handleExpiredAuction(roomId, "BID_FAIL");
            }

            long remainingSeconds = Duration.between(now, scheduledEnd).getSeconds();
            applyFinalWindowRules(state, now, remainingSeconds);

            if (!state.hasParticipant(userId)) {
                return fail("BID_FAIL", "Bạn không nằm trong danh sách người tham gia hợp lệ của phiên!");
            }

            BidDAO bidDAO = new BidDAO();
            boolean bidSuccess = bidDAO.placeBid(roomId, userId, amount);

            if (!bidSuccess) {
                return fail("BID_FAIL", "Giá phải cao hơn mức hiện tại!");
            }

            room.setCurrentPrice(amount);
            room.setHighestBidder(user.getUsername());

            boolean extended = false;
            if (remainingSeconds <= FINAL_WINDOW_SECONDS) {
                int extensionSeconds = room.getExtensionSeconds();
                if (extensionSeconds > 0) {
                    state.extendBySeconds(extensionSeconds);
                    extended = true;
                }
            }

            syncRuntimeInfoToRoom(room, state);

            return new Message(
                    extended ? "BID_SUCCESS_EXTENDED" : "BID_SUCCESS",
                    user.getUsername(),
                    room
            );
        }
    }

    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        if (sellerName == null || sellerName.isBlank()) {
            return false;
        }
        if (itemName == null || itemName.isBlank()) {
            return false;
        }
        return startingPrice > 0;
    }

    // =========================
    // STATUS CHECKS WITH SWITCH
    // =========================

    private Message validateJoinByStatus(AuctionRoom room, LocalDateTime now) {
        String status = normalizeStatus(room.getStatus());

        switch (status) {
            case "SOLD":
                return fail("ROOM_FAIL", "Phiên đấu giá này đã bán thành công và không còn khả dụng!");

            case "UNSOLD":
                return fail("ROOM_FAIL", "Phiên đấu giá này đã kết thúc mà không có người mua!");

            case "ENDED":
            case "CLOSED_BY_SELLER":
                return fail("ROOM_FAIL", "Phiên đấu giá này đã được người bán đóng!");

            case "CANCELED":
            case "CANCELED_BY_ADMIN":
                return fail("ROOM_FAIL", "Phiên đấu giá này đã bị hủy!");

            case "OPEN":
            case "RUNNING":
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    return fail("ROOM_FAIL", "Phiên đấu giá chưa bắt đầu!");
                }
                return null;

            default:
                return fail("ROOM_FAIL", "Trạng thái phiên đấu giá không hợp lệ: " + status);
        }
    }

    private Message validateBidByStatus(AuctionRoom room, LocalDateTime now) {
        String status = normalizeStatus(room.getStatus());

        switch (status) {
            case "SOLD":
                return fail("BID_FAIL", "Phiên đấu giá đã bán thành công, không thể đặt giá!");

            case "UNSOLD":
                return fail("BID_FAIL", "Phiên đấu giá đã kết thúc mà không có người mua!");

            case "ENDED":
            case "CLOSED_BY_SELLER":
                return fail("BID_FAIL", "Phiên đấu giá đã được người bán đóng!");

            case "CANCELED":
            case "CANCELED_BY_ADMIN":
                return fail("BID_FAIL", "Phiên đấu giá đã bị hủy!");

            case "OPEN":
            case "RUNNING":
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    return fail("BID_FAIL", "Phiên đấu giá chưa bắt đầu!");
                }
                return null;

            default:
                return fail("BID_FAIL", "Trạng thái phiên đấu giá không hợp lệ: " + status);
        }
    }

    // =========================
    // TIMEOUT / SETTLEMENT
    // =========================

    private Message handleExpiredAuction(String roomId, String failAction) {
        AuctionDAO auctionDAO = new AuctionDAO();
        CloseAuctionResult result = auctionDAO.closeAuctionByTime(roomId);

        AuctionStateManager.removeState(roomId);

        broadcastBalancesAfterTimeout(result);
        AuctionServer.broadcastToRoom(roomId, new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId));
        broadcastRoomList();

        return fail(failAction, result.getMessage());
    }

    private void broadcastBalancesAfterTimeout(CloseAuctionResult result) {
        if (result == null || !result.isSuccess()) return;
        if (!"SOLD".equalsIgnoreCase(result.getFinalStatus())) return;

        if (result.getWinnerId() != null && result.getWinnerBalance() != null) {
            AuctionServer.broadcastAll(
                    new Message("UPDATE_BALANCE", result.getWinnerId(), result.getWinnerBalance())
            );
        }

        if (result.getSellerId() != null && result.getSellerBalance() != null) {
            AuctionServer.broadcastAll(
                    new Message("UPDATE_BALANCE", result.getSellerId(), result.getSellerBalance())
            );
        }
    }

    private void broadcastRoomList() {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionServer.broadcastAll(
                new Message("ROOM_LIST", "SERVER", auctionDAO.getAllActiveAuctions())
        );
    }

    // =========================
    // RUNTIME / TIME HELPERS
    // =========================

    private void applyFinalWindowRules(AuctionRuntimeState state, LocalDateTime now, long remainingSeconds) {
        if (!state.isEntryLocked() && remainingSeconds <= FINAL_WINDOW_SECONDS) {
            state.lockEntry(now);
        }
    }

    private LocalDateTime calculateScheduledEnd(AuctionRoom room, AuctionRuntimeState state) {
        if (room == null || room.getEndTime() == null) {
            return null;
        }
        return room.getEndTime().plusSeconds(state.getTotalExtendedSeconds());
    }

    private void syncRuntimeInfoToRoom(AuctionRoom room, AuctionRuntimeState state) {
        room.setExtendedSeconds(state.getTotalExtendedSeconds());
        room.setEntryLocked(state.isEntryLocked());
        room.setScheduledEndTime(calculateScheduledEnd(room, state));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNKNOWN";
        }
        return status.trim().toUpperCase();
    }

    private Message fail(String action, String content) {
        return new Message(action, "SERVER", content);
    }
}