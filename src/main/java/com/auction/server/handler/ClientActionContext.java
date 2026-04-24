package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.AuthService;
import com.auction.server.service.PendingAuctionApprovalService;

import java.util.Objects;
import java.util.function.Consumer;

public class ClientActionContext {
    private final Consumer<Message> responder;
    private final ServerEventPublisher eventPublisher;
    private final AuthService authService;
    private final AuctionRoomService roomService;
    private final PendingAuctionApprovalService pendingAuctionApprovalService;

    private volatile String currentRoomId = "";
    private volatile String userId = "";

    public ClientActionContext(
            Consumer<Message> responder,
            ServerEventPublisher eventPublisher,
            AuthService authService,
            AuctionRoomService roomService,
            PendingAuctionApprovalService pendingAuctionApprovalService
    ) {
        this.responder = Objects.requireNonNull(responder);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.authService = Objects.requireNonNull(authService);
        this.roomService = Objects.requireNonNull(roomService);
        this.pendingAuctionApprovalService = Objects.requireNonNull(pendingAuctionApprovalService);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AuctionRoomService getRoomService() {
        return roomService;
    }

    public PendingAuctionApprovalService getPendingAuctionApprovalService() {
        return pendingAuctionApprovalService;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId == null ? "" : currentRoomId;
    }

    public void clearCurrentRoom() {
        this.currentRoomId = "";
    }

    public void leaveCurrentRoomIfMatches(String roomId) {
        if (roomId != null && roomId.equals(currentRoomId)) {
            clearCurrentRoom();
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? "" : userId;
    }

    public void send(Message message) {
        responder.accept(message);
    }

    public void broadcastAll(Message message) {
        eventPublisher.broadcastAll(message);
    }

    public void broadcastToRoom(String roomId, Message message) {
        eventPublisher.broadcastToRoom(roomId, message);
    }

    public void notifyRoomClosed(String roomId) {
        eventPublisher.notifyRoomClosed(roomId);
    }

    public void notifyDeletedUser(String userId) {
        eventPublisher.notifyDeletedUser(userId);
    }
}
