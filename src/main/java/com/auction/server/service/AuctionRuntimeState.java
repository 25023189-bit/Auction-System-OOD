package com.auction.server.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Trạng thái runtime của một phòng đấu giá, chỉ lưu trong bộ nhớ server.
 * Dùng cho phần gia hạn thời gian, khóa vào phòng cuối giờ và danh sách participant.
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
