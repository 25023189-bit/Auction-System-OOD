package com.auction.client.shared.mapper;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.common.model.AuctionRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper chuyển danh sách AuctionRoom sang model hiển thị lobby.
 *
 * Vai trò:
 * - Rút gọn dữ liệu phòng server thành LobbyRoomDisplayModel.
 * - Format dữ liệu cần thiết cho card lobby như roomId, itemName và currentPrice.
 *
 * Luồng chính:
 * 1. AdvancedLobbyMessageHandler nhận List<AuctionRoom> từ server.
 * 2. Mapper tạo List<LobbyRoomDisplayModel> để LobbyRoomListRenderer render card.
 *
 * Business rules:
 * - Source null trả về danh sách rỗng để UI không lỗi.
 * - Lobby chỉ lấy các trường cần hiển thị, không mang toàn bộ trạng thái room vào card.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ tạo list/model mới.
 * - Dependency: DisplayMapper, AuctionRoom, LobbyRoomDisplayModel, List.
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
                    room.getCurrentPrice(),
                    room.getBase64Image()
            ));
        }
        return result;
    }
}
