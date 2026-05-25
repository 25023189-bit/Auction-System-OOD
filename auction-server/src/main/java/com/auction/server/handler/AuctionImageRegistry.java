package com.auction.server.handler;

import com.auction.common.model.AuctionRoom;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AuctionImageRegistry {
    private static final ConcurrentMap<String, String> IMAGES_BY_ROOM_ID = new ConcurrentHashMap<>();

    private AuctionImageRegistry() {
    }

    public static void put(String roomId, String base64Image) {
        if (roomId == null || roomId.isBlank() || base64Image == null || base64Image.isBlank()) {
            return;
        }
        IMAGES_BY_ROOM_ID.put(roomId, base64Image);
    }

    public static void apply(AuctionRoom room) {
        if (room == null || room.getRoomId() == null) {
            return;
        }
        String base64Image = IMAGES_BY_ROOM_ID.get(room.getRoomId());
        if (base64Image != null && !base64Image.isBlank()) {
            room.setBase64Image(base64Image);
        }
    }

    public static void applyAll(List<AuctionRoom> rooms) {
        if (rooms == null) {
            return;
        }
        rooms.forEach(AuctionImageRegistry::apply);
    }

    public static void remove(String roomId) {
        if (roomId != null) {
            IMAGES_BY_ROOM_ID.remove(roomId);
        }
    }
}
