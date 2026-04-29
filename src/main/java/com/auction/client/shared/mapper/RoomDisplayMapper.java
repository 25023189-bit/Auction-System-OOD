package com.auction.client.shared.mapper;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.common.model.AuctionRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Map model phòng từ server sang model gọn hơn để hiển thị ở lobby.
 */
public class RoomDisplayMapper implements DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> {
    @Override
    public List<LobbyRoomDisplayModel> map(List<AuctionRoom> source) {
        List<LobbyRoomDisplayModel> result = new ArrayList<>();
        if (source == null) return result;

        for (AuctionRoom room : source) {
            // Lobby chỉ cần id, tên sản phẩm và giá hiện tại.
            String displayName = room.getItemName();

            result.add(new LobbyRoomDisplayModel(
                    room.getRoomId(),
                    displayName,
                    room.getCurrentPrice()
            ));
        }
        return result;
    }
}
