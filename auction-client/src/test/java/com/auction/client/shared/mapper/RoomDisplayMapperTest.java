package com.auction.client.shared.mapper;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.common.model.AuctionRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomDisplayMapperTest {

    private RoomDisplayMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoomDisplayMapper();
    }

    @Test
    @DisplayName("Test xử lý an toàn: Trả về danh sách rỗng khi nguồn dữ liệu bị null")
    void testMap_NullSource_ReturnsEmptyList() {
        List<LobbyRoomDisplayModel> result = mapper.map(null);

        assertNotNull(result, "Kết quả không được trả về null");
        assertTrue(result.isEmpty(), "Kết quả phải là một danh sách rỗng");
    }

    @Test
    @DisplayName("Test xử lý an toàn: Trả về danh sách rỗng khi nguồn dữ liệu trống")
    void testMap_EmptyListSource_ReturnsEmptyList() {
        List<LobbyRoomDisplayModel> result = mapper.map(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test chuyển đổi chính xác dữ liệu từ Server sang giao diện Card Lobby")
    void testMap_ValidSource_ReturnsMappedList() {
        // 1. Tạo danh sách phòng mẫu (Giả lập dữ liệu Server gửi về)
        List<AuctionRoom> sourceList = new ArrayList<>();

        AuctionRoom room1 = new AuctionRoom();
        // LƯU Ý: Nếu model của Hân dùng hàm setId() thay vì setRoomId(), nhớ sửa lại nhé!
        room1.setRoomId("ROOM_01");
        room1.setItemName("Laptop Alienware");
        room1.setCurrentPrice(2500.0);

        AuctionRoom room2 = new AuctionRoom();
        room2.setRoomId("ROOM_02");
        room2.setItemName("Bàn phím cơ cơ học");
        room2.setCurrentPrice(150.0);

        sourceList.add(room1);
        sourceList.add(room2);

        // 2. Gọi class Mapper để chuyển đổi
        List<LobbyRoomDisplayModel> result = mapper.map(sourceList);

        // 3. Xác thực số lượng không bị mất mát
        assertEquals(2, result.size(), "Phải chuyển đổi đủ 2 phòng");

        // 4. Xác thực dữ liệu phòng 1
        LobbyRoomDisplayModel model1 = result.get(0);
        // LƯU Ý 2: Nếu LobbyRoomDisplayModel là dạng Java Record, hãy đổi .getRoomId() thành .roomId()
        assertEquals("ROOM_01", model1.getRoomId());
        assertEquals("Laptop Alienware", model1.getItemName());
        assertEquals(2500.0, model1.getCurrentPrice());

        // 5. Xác thực dữ liệu phòng 2
        LobbyRoomDisplayModel model2 = result.get(1);
        assertEquals("ROOM_02", model2.getRoomId());
        assertEquals("Bàn phím cơ cơ học", model2.getItemName());
        assertEquals(150.0, model2.getCurrentPrice());
    }
}
