package com.auction.server.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class AuctionRuntimeState {
    private long totalExtendedSeconds = 0;
    private boolean entryLocked = false;
    private LocalDateTime entryLockedAt;
    private final Set<String> participants = new HashSet<>();

    public long getTotalExtendedSeconds() {
        return totalExtendedSeconds;
    }

    public void extendBySeconds(long seconds) {
        this.totalExtendedSeconds += seconds;
    }

    public boolean isEntryLocked() {
        return entryLocked;
    }

    public void lockEntry(LocalDateTime now) {
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