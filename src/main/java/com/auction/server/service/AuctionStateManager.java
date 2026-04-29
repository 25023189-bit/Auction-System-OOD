package com.auction.server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AuctionStateManager {
    private static final ConcurrentMap<String, AuctionRuntimeState> STATES = new ConcurrentHashMap<>();

    private AuctionStateManager() {
    }

    public static AuctionRuntimeState getState(String roomId) {
        return STATES.computeIfAbsent(roomId, id -> new AuctionRuntimeState());
    }

    public static void removeState(String roomId) {
        STATES.remove(roomId);
    }
}