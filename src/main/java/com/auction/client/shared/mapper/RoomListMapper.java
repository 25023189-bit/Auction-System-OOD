package com.auction.client.shared.mapper;

import com.auction.common.model.AuctionRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse danh sách phòng dạng chuỗi legacy: roomId|itemName|price;...
 */
public class RoomListMapper implements DisplayMapper<String, List<AuctionRoom>> {
    @Override
    public List<AuctionRoom> map(String rawData) {
        List<AuctionRoom> rooms = new ArrayList<>();

        if (rawData == null || rawData.trim().isEmpty()) {
            return rooms;
        }

        // Mỗi phòng được ngăn bằng dấu ;, mỗi thuộc tính trong phòng ngăn bằng dấu |.
        String[] roomEntries = rawData.split(";");
        for (String entry : roomEntries) {
            if (entry == null || entry.isBlank()) continue;

            String[] info = entry.split("\\|");
            if (info.length >= 3) {
                String roomId = info[0];
                String itemName = info[1];
                double currentPrice = Double.parseDouble(info[2]);

                AuctionRoom room = new AuctionRoom();
                room.setRoomId(roomId);
                room.setItemName(itemName);
                room.setCurrentPrice(currentPrice);
                rooms.add(room);
            }
        }

        return rooms;
    }
}
