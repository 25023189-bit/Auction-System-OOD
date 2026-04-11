package com.auction.client.shared.mapper;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.common.model.AuctionRoom;

import java.util.ArrayList;
import java.util.List;

public class RoomDisplayMapper implements DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> {
    @Override
    public List<LobbyRoomDisplayModel> map(List<AuctionRoom> source) {
        List<LobbyRoomDisplayModel> result = new ArrayList<>();
        if (source == null) return result;

        for (AuctionRoom room : source) {
            result.add(new LobbyRoomDisplayModel(
                    room.getRoomId(),
                    room.getItemName(),
                    room.getCurrentPrice()
            ));
        }
        return result;
    }
}