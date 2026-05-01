package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.AuthService;
import com.auction.server.service.PendingAuctionApprovalService;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Ngữ cảnh xử lý gắn với một client connection.
 *
 * Vai trò:
 * - Cung cấp responder, event publisher và các service cần dùng cho handler.
 * - Lưu userId/currentRoomId của phiên client hiện tại.
 *
 * Luồng chính:
 * 1. ClientHandler tạo context khi mở socket và truyền vào router/handler.
 * 2. Handler đọc/ghi trạng thái phiên, gửi response hoặc phát event qua context.
 *
 * Business rules:
 * - currentRoomId phải được xóa khi client rời phòng hoặc phòng bị đóng.
 * - userId chỉ được set sau khi login thành công để các action có thể kiểm tra danh tính.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: currentRoomId và userId là volatile, nhưng service bên trong không được bảo vệ thêm.
 * - Dependency: Consumer<Message>, ServerEventPublisher, AuthService, AuctionRoomService, PendingAuctionApprovalService.
 */
public class ClientActionContext {
    // responder gửi message trực tiếp về client hiện tại.
    private final Consumer<Message> responder;
    // eventPublisher dùng cho broadcast toàn hệ thống hoặc theo phòng.
    private final ServerEventPublisher eventPublisher;
    private final AuthService authService;
    private final AuctionRoomService roomService;
    private final PendingAuctionApprovalService pendingAuctionApprovalService;

    // volatile vì trạng thái này có thể được đọc khi server broadcast/đóng phòng từ thread khác.
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
        // Dùng khi phòng bị đóng để chỉ xóa session nếu client đang ở đúng phòng đó.
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
