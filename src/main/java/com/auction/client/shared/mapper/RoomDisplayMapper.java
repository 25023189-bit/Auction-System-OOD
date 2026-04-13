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
            // Thay vì getItemName() đã bị xóa, ta hiển thị ID Sản phẩm để Client không bị lỗi
            String displayName = "Sản phẩm #" + room.getProductId();

            result.add(new LobbyRoomDisplayModel(
                    room.getAuctionId(), // Đã đổi roomId -> auctionId
                    displayName,
                    room.getCurrentPrice()
            ));
        }
        return result;
    }
}