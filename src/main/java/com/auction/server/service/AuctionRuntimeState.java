package com.auction.server.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Trạng thái runtime trong bộ nhớ của một phòng đấu giá.
 *
 * Vai trò:
 * - Lưu tổng thời gian đã gia hạn, trạng thái khóa người mới và danh sách participant.
 * - Cung cấp dữ liệu bổ sung để AuctionRoomService tính scheduled end time và quyền bid.
 *
 * Luồng chính:
 * 1. AuctionStateManager tạo hoặc trả state theo roomId khi có join/bid/finalize.
 * 2. AuctionRoomService synchronized trên state rồi đọc/ghi extension, lock và participant.
 *
 * Business rules:
 * - Participant phải được add sau khi join hợp lệ thì mới được bid.
 * - Entry chỉ ghi nhận khóa lần đầu khi phòng bước vào final window.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe tự thân: caller phải đồng bộ bên ngoài khi đọc/ghi state.
 * - Dependency: LocalDateTime, Set/HashSet; được quản lý bởi AuctionStateManager.
 */
public class AuctionRuntimeState {
    private long totalExtendedSeconds = 0;
    private boolean entryLocked = false;
    private LocalDateTime entryLockedAt;
    // Set participant giúp server biết ai đã join hợp lệ trước khi cho bid.
    private final Set<String> participants = new HashSet<>();

    public long getTotalExtendedSeconds() {
        return totalExtendedSeconds;
    }

    public void extendBySeconds(long seconds) {
        // Tổng gia hạn được cộng dồn để tính scheduledEndTime hiện tại.
        this.totalExtendedSeconds += seconds;
    }

    public boolean isEntryLocked() {
        return entryLocked;
    }

    public void lockEntry(LocalDateTime now) {
        // Chỉ ghi nhận thời điểm khóa lần đầu.
        if (!entryLocked) {
            entryLocked = true;
            entryLockedAt = now;
        }
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public boolean hasParticipant(String userId) {
        return participants.contains(userId);
    }

    public void addParticipant(String userId) {
        if (userId != null && !userId.isBlank()) {
            participants.add(userId);
        }
    }
}
