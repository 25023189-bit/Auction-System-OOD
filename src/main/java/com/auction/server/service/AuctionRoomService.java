package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.AuctionDAO.CloseAuctionResult;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.BidDAO.BidResult;
import com.auction.server.dao.UserDAO;
import com.auction.server.main.AuctionServer;

import java.time.Duration;
import java.time.LocalDateTime;

public class AuctionRoomService {
    private static final long FINAL_WINDOW_SECONDS = 30L;

    public Message joinRoom(String roomId, String userId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return fail("ROOM_FAIL", "Khong tim thay phong hoac phien da ket thuc!");
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return fail("ROOM_FAIL", "Khong tim thay nguoi dung!");
        }

        LocalDateTime now = LocalDateTime.now();
        Message preCheck = validateJoinByStatus(room, user, now);
        if (preCheck != null) {
            return preCheck;
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);
            if (scheduledEnd == null) {
                return fail("ROOM_FAIL", "Phien dau gia thieu thong tin thoi gian!");
            }

            if (!now.isBefore(scheduledEnd)) {
                return handleExpiredAuction(roomId, "ROOM_FAIL");
            }

            long remainingSeconds = Duration.between(now, scheduledEnd).getSeconds();
            applyFinalWindowRules(state, now, remainingSeconds);

            if (state.isEntryLocked() && !state.hasParticipant(userId)) {
                return fail("ROOM_FAIL", "Phien da khoa nguoi tham gia moi trong 30 giay cuoi!");
            }

            if ("BIDDER".equalsIgnoreCase(user.getRole()) && user.getBalance() < room.getMinimumJoinAmount()) {
                return fail("ROOM_FAIL", "So du cua ban chua dat muc toi thieu de tham gia phien nay!");
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
            return fail("BID_FAIL", "Khong tim thay nguoi dung!");
        }

        if (!"BIDDER".equalsIgnoreCase(user.getRole())) {
            return fail("BID_FAIL", "Ban khong phai nguoi mua de tham gia dat gia!");
        }

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return fail("BID_FAIL", "Khong tim thay phong dau gia hoac phien da ket thuc!");
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
                return fail("BID_FAIL", "Phien dau gia thieu thong tin thoi gian!");
            }

            if (!now.isBefore(scheduledEnd)) {
                return handleExpiredAuction(roomId, "BID_FAIL");
            }

            long remainingSeconds = Duration.between(now, scheduledEnd).getSeconds();
            applyFinalWindowRules(state, now, remainingSeconds);

            if (!state.hasParticipant(userId)) {
                return fail("BID_FAIL", "Ban khong nam trong danh sach nguoi tham gia hop le cua phien!");
            }

            BidDAO bidDAO = new BidDAO();
            BidResult bidResult = bidDAO.placeBid(roomId, userId, amount);
            if (!bidResult.isSuccess()) {
                return fail("BID_FAIL", bidResult.getMessage() != null
                        ? bidResult.getMessage()
                        : "Khong the dat gia.");
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

    public boolean finalizeExpiredAuctionIfNeeded(String roomId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);
        if (room == null) {
            return false;
        }

        String status = normalizeStatus(room.getStatus());
        if (!"OPEN".equals(status) && !"RUNNING".equals(status)) {
            return false;
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);
        synchronized (state) {
            LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);
            if (scheduledEnd == null) {
                return false;
            }
            if (LocalDateTime.now().isBefore(scheduledEnd)) {
                return false;
            }

            finalizeAuction(roomId);
            return true;
        }
    }

    private Message validateJoinByStatus(AuctionRoom room, User user, LocalDateTime now) {
        String status = normalizeStatus(room.getStatus());

        switch (status) {
            case "SOLD":
                return fail("ROOM_FAIL", "Phien dau gia nay da ban thanh cong va khong con kha dung!");
            case "UNSOLD":
                return fail("ROOM_FAIL", "Phien dau gia nay da ket thuc ma khong co nguoi mua!");
            case "ENDED":
            case "CLOSED_BY_SELLER":
                return fail("ROOM_FAIL", "Phien dau gia nay da duoc nguoi ban dong!");
            case "CANCELED":
            case "CANCELED_BY_ADMIN":
                return fail("ROOM_FAIL", "Phien dau gia nay da bi huy!");
            case "OPEN":
            case "RUNNING":
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    if (user == null || user.getId() == null || !user.getId().equalsIgnoreCase(room.getSellerName())) {
                        return fail("ROOM_FAIL", "Chi seller cua phien moi duoc vao truoc khi phien bat dau!");
                    }
                }
                return null;
            default:
                return fail("ROOM_FAIL", "Trang thai phien dau gia khong hop le: " + status);
        }
    }

    private Message validateBidByStatus(AuctionRoom room, LocalDateTime now) {
        String status = normalizeStatus(room.getStatus());

        switch (status) {
            case "SOLD":
                return fail("BID_FAIL", "Phien dau gia da ban thanh cong, khong the dat gia!");
            case "UNSOLD":
                return fail("BID_FAIL", "Phien dau gia da ket thuc ma khong co nguoi mua!");
            case "ENDED":
            case "CLOSED_BY_SELLER":
                return fail("BID_FAIL", "Phien dau gia da duoc nguoi ban dong!");
            case "CANCELED":
            case "CANCELED_BY_ADMIN":
                return fail("BID_FAIL", "Phien dau gia da bi huy!");
            case "OPEN":
            case "RUNNING":
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    return fail("BID_FAIL", "Phien dau gia chua bat dau!");
                }
                return null;
            default:
                return fail("BID_FAIL", "Trang thai phien dau gia khong hop le: " + status);
        }
    }

    private Message handleExpiredAuction(String roomId, String failAction) {
        CloseAuctionResult result = finalizeAuction(roomId);
        return fail(failAction, result.getMessage());
    }

    private CloseAuctionResult finalizeAuction(String roomId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        CloseAuctionResult result = auctionDAO.closeAuctionByTime(roomId);

        AuctionStateManager.removeState(roomId);
        broadcastBalancesAfterTimeout(result);
        AuctionServer.notifyRoomClosed(roomId);
        broadcastRoomList();

        return result;
    }

    private void broadcastBalancesAfterTimeout(CloseAuctionResult result) {
        if (result == null || !result.isSuccess()) {
            return;
        }
        if (!"SOLD".equalsIgnoreCase(result.getFinalStatus())) {
            return;
        }

        if (result.getWinnerId() != null && result.getWinnerBalance() != null) {
            AuctionServer.broadcastAll(new Message("UPDATE_BALANCE", result.getWinnerId(), result.getWinnerBalance()));
        }

        if (result.getSellerId() != null && result.getSellerBalance() != null) {
            AuctionServer.broadcastAll(new Message("UPDATE_BALANCE", result.getSellerId(), result.getSellerBalance()));
        }
    }

    private void broadcastRoomList() {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", auctionDAO.getAllActiveAuctions()));
    }

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
