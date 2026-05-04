package com.auction.client.shared.mapper;

import com.auction.common.model.AuctionRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper parse danh sách phòng dạng chuỗi legacy sang AuctionRoom.
 *
 * Vai trò:
 * - Hỗ trợ response ROOM_LIST cũ có format roomId|itemName|price;...
 * - Tạo AuctionRoom tối thiểu để các renderer cũ vẫn hiển thị được lobby.
 *
 * Luồng chính:
 * 1. LobbyMessageHandler hoặc AdvancedLobbyMessageHandler truyền rawData dạng String.
 * 2. Mapper tách từng entry, parse giá và trả List<AuctionRoom>.
 *
 * Business rules:
 * - rawData null/rỗng trả về danh sách rỗng.
 * - Entry không đủ 3 field bị bỏ qua; entry có giá không parse được sẽ làm caller nhận lỗi parse.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ dùng biến local.
 * - Dependency: DisplayMapper, AuctionRoom, List/ArrayList.
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
