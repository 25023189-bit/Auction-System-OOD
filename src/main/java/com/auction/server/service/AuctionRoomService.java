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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service nghiệp vụ điều phối trạng thái phòng đấu giá trong runtime.
 *
 * Vai trò:
 * - Xử lý join room, đặt bid, khóa người mới vào cuối phiên và finalize phiên hết hạn.
 * - Đồng bộ dữ liệu runtime trong bộ nhớ với AuctionRoom trả về client.
 *
 * Luồng chính:
 * 1. Handler gọi joinRoom(), placeNewBid() hoặc watcher gọi finalizeExpiredAuctionIfNeeded().
 * 2. Service đọc DB, kiểm tra status/time/runtime state, gọi DAO transaction rồi broadcast các event cần thiết.
 *
 * Business rules:
 * - Trong 30 giây cuối, người mới bị khóa vào phòng nhưng participant đã join vẫn được bid.
 * - Bid hợp lệ trong 30 giây cuối được gia hạn thêm theo cấu hình extensionSeconds của room.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe theo room: synchronized trên AuctionRuntimeState từng room để tránh join/bid/finalize đụng nhau.
 * - Dependency: AuctionDAO, BidDAO, UserDAO, AuctionStateManager, AuctionServer, Message.
 */
public class AuctionRoomService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionRoomService.class);
    private static final long FINAL_WINDOW_SECONDS = 30L;

    public Message joinRoom(String roomId, String userId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return fail("ROOM_FAIL", "Room not found or the auction has ended.");
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return fail("ROOM_FAIL", "User not found.");
        }

        LocalDateTime now = LocalDateTime.now();
        // Kiểm tra trạng thái DB trước khi đụng tới runtime state trong bộ nhớ.
        Message preCheck = validateJoinByStatus(room, user, now);
        if (preCheck != null) {
            return preCheck;
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);
            if (scheduledEnd == null) {
                return fail("ROOM_FAIL", "Auction schedule information is missing.");
            }

            if (!now.isBefore(scheduledEnd)) {
                return handleExpiredAuction(roomId, "ROOM_FAIL");
            }

            long remainingSeconds = Duration.between(now, scheduledEnd).getSeconds();
            // Khi vào 30 giây cuối, server khóa người tham gia mới.
            applyFinalWindowRules(state, now, remainingSeconds);

            if (state.isEntryLocked() && !state.hasParticipant(userId)) {
                return fail("ROOM_FAIL", "New participants are locked during the final 30 seconds.");
            }

            if ("BIDDER".equalsIgnoreCase(user.getRole()) && user.getBalance() < room.getMinimumJoinAmount()) {
                return fail("ROOM_FAIL", "Your balance does not meet the minimum join amount for this auction.");
            }

            // Chỉ user đã join hợp lệ mới được phép bid trong phiên hiện tại.
            state.addParticipant(userId);
            syncRuntimeInfoToRoom(room, state);

            return new Message("ROOM_JOINED", "SERVER", room);
        }
    }

    public Message placeNewBid(String roomId, String userId, double amount) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        if (user == null) {
            return fail("BID_FAIL", "User not found.");
        }

        if (!"BIDDER".equalsIgnoreCase(user.getRole())) {
            return fail("BID_FAIL", "Only bidders can place bids.");
        }

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return fail("BID_FAIL", "Auction room not found or the auction has ended.");
        }

        LocalDateTime now = LocalDateTime.now();
        // Server validate lại thời gian/trạng thái dù client đã chặn UI.
        Message preCheck = validateBidByStatus(room, now);
        if (preCheck != null) {
            return preCheck;
        }

        AuctionRuntimeState state = AuctionStateManager.getState(roomId);

        synchronized (state) {
            LocalDateTime scheduledEnd = calculateScheduledEnd(room, state);
            if (scheduledEnd == null) {
                return fail("BID_FAIL", "Auction schedule information is missing.");
            }

            if (!now.isBefore(scheduledEnd)) {
                return handleExpiredAuction(roomId, "BID_FAIL");
            }

            long remainingSeconds = Duration.between(now, scheduledEnd).getSeconds();
            applyFinalWindowRules(state, now, remainingSeconds);

            if (!state.hasParticipant(userId)) {
                return fail("BID_FAIL", "You are not in the valid participant list for this auction.");
            }

            BidDAO bidDAO = new BidDAO();
            // DAO xử lý transaction: clear highest cũ, insert bid mới, update current price.
            BidResult bidResult = bidDAO.placeBid(roomId, userId, amount);
            if (!bidResult.isSuccess()) {
                return fail("BID_FAIL", bidResult.getMessage() != null
                        ? bidResult.getMessage()
                        : "Unable to place bid.");
            }

            room.setCurrentPrice(amount);
            room.setHighestBidder(user.getUsername());

            boolean extended = false;
            // Bid trong 30 giây cuối được gia hạn thêm theo cấu hình của phòng.
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
        // Đồng bộ theo từng phòng để watcher không finalize trùng với bid/join đang xử lý.
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
                return fail("ROOM_FAIL", "This auction has been sold and is no longer available.");
            case "UNSOLD":
                return fail("ROOM_FAIL", "This auction ended without a buyer.");
            case "ENDED":
            case "CLOSED_BY_SELLER":
                return fail("ROOM_FAIL", "This auction was closed by the seller.");
            case "CANCELED":
            case "CANCELED_BY_ADMIN":
                return fail("ROOM_FAIL", "This auction has been canceled.");
            case "OPEN":
            case "RUNNING":
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    if (user == null || user.getId() == null || !user.getId().equalsIgnoreCase(room.getSellerName())) {
                        return fail("ROOM_FAIL", "Only this auction's seller can enter before the auction starts.");
                    }
                }
                return null;
            default:
                return fail("ROOM_FAIL", "Invalid auction status: " + status);
        }
    }

    private Message validateBidByStatus(AuctionRoom room, LocalDateTime now) {
        String status = normalizeStatus(room.getStatus());

        switch (status) {
            case "SOLD":
                return fail("BID_FAIL", "This auction has been sold. Bidding is not available.");
            case "UNSOLD":
                return fail("BID_FAIL", "This auction ended without a buyer.");
            case "ENDED":
            case "CLOSED_BY_SELLER":
                return fail("BID_FAIL", "This auction was closed by the seller.");
            case "CANCELED":
            case "CANCELED_BY_ADMIN":
                return fail("BID_FAIL", "This auction has been canceled.");
            case "OPEN":
            case "RUNNING":
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    return fail("BID_FAIL", "This auction has not started yet.");
                }
                return null;
            default:
                return fail("BID_FAIL", "Invalid auction status: " + status);
        }
    }

    // Nếu client thao tác vào phiên đã hết giờ, server finalize trước rồi trả lỗi phù hợp.
    private Message handleExpiredAuction(String roomId, String failAction) {
        CloseAuctionResult result = finalizeAuction(roomId);
        return fail(failAction, result.getMessage());
    }

    // Kết thúc phiên: cập nhật DB, dọn runtime state, báo client và refresh lobby.
    private CloseAuctionResult finalizeAuction(String roomId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        CloseAuctionResult result = auctionDAO.closeAuctionByTime(roomId);

        if (result == null || !result.isSuccess()) {
            LOGGER.warn(
                    "Auction {} reached its end time but could not be finalized: {}",
                    roomId,
                    result != null ? result.getMessage() : "Unknown finalization error."
            );
            return result != null
                    ? result
                    : CloseAuctionResult.fail("Unable to finalize auction.");
        }

        AuctionStateManager.removeState(roomId);
        broadcastBalancesAfterTimeout(result);
        AuctionServer.notifyRoomClosed(roomId);
        broadcastRoomList();

        return result;
    }

    // Sau khi bán thành công, winner và seller cần thấy số dư mới.
    private void broadcastBalancesAfterTimeout(CloseAuctionResult result) {
        if (result == null || !result.isSuccess()) {
            return;
        }
        if (!"SOLD".equalsIgnoreCase(result.getFinalStatus())) {
            return;
        }

        if (result.getWinnerId() != null && result.getWinnerBalance() != null) {
            AuctionServer.broadcast(new Message("UPDATE_BALANCE", result.getWinnerId(), result.getWinnerBalance()));
        }

        if (result.getSellerId() != null && result.getSellerBalance() != null) {
            AuctionServer.broadcast(new Message("UPDATE_BALANCE", result.getSellerId(), result.getSellerBalance()));
        }
    }

    private void broadcastRoomList() {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionServer.broadcast(new Message("ROOM_LIST", "SERVER", auctionDAO.getAllActiveAuctions()));
    }

    // Rule 30 giây cuối: khóa người mới vào phòng nhưng người đã join vẫn được bid.
    private void applyFinalWindowRules(AuctionRuntimeState state, LocalDateTime now, long remainingSeconds) {
        if (!state.isEntryLocked() && remainingSeconds <= FINAL_WINDOW_SECONDS) {
            state.lockEntry(now);
        }
    }

    // End time thực tế = endTime gốc trong DB + tổng số giây đã được gia hạn.
    private LocalDateTime calculateScheduledEnd(AuctionRoom room, AuctionRuntimeState state) {
        if (room == null || room.getEndTime() == null) {
            return null;
        }
        return room.getEndTime().plusSeconds(state.getTotalExtendedSeconds());
    }

    // Đưa runtime state vào AuctionRoom để client hiển thị đúng timer, lock và số người tham gia.
    private void syncRuntimeInfoToRoom(AuctionRoom room, AuctionRuntimeState state) {
        room.setExtendedSeconds(state.getTotalExtendedSeconds());
        room.setEntryLocked(state.isEntryLocked());
        room.setScheduledEndTime(calculateScheduledEnd(room, state));
        room.setParticipantCount(state.getParticipants().size());
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
