package com.auction.client.shared.mapper;

import com.auction.common.model.AuctionRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomListMapperTest {

    private RoomListMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoomListMapper();
    }

    @Test
    @DisplayName("Test chuỗi rỗng hoặc null -> Trả về danh sách rỗng")
    void testMap_NullOrEmptyString_ReturnsEmptyList() {
        assertTrue(mapper.map(null).isEmpty(), "Null phải trả về danh sách rỗng");
        assertTrue(mapper.map("").isEmpty(), "Chuỗi rỗng phải trả về danh sách rỗng");
        assertTrue(mapper.map("   ").isEmpty(), "Chuỗi toàn khoảng trắng phải trả về danh sách rỗng");
    }

    @Test
    @DisplayName("Test phân giải thành công một chuỗi chuẩn (1 phòng)")
    void testMap_ValidSingleRoom_ReturnsMappedList() {
        String rawData = "ROOM_01|Laptop Dell|1200.5";
        List<AuctionRoom> result = mapper.map(rawData);

        assertEquals(1, result.size());

        AuctionRoom room = result.get(0);
        assertEquals("ROOM_01", room.getRoomId()); // Lưu ý: Đổi thành getId() nếu model dùng getId()
        assertEquals("Laptop Dell", room.getItemName());
        assertEquals(1200.5, room.getCurrentPrice());
    }

    @Test
    @DisplayName("Test phân giải chuỗi có nhiều phòng và dư dấu chấm phẩy (;)")
    void testMap_MultipleRoomsWithExtraSemicolons_ReturnsMappedList() {
        // Chuỗi bị dư dấu ; ở giữa và ở cuối (Rất hay gặp khi code thực tế)
        String rawData = "ROOM_01|Laptop Dell|1200.5;;ROOM_02|Chuột Razer|50.0;";
        List<AuctionRoom> result = mapper.map(rawData);

        assertEquals(2, result.size(), "Phải bỏ qua các khoảng trống và lấy đủ 2 phòng");

        assertEquals("ROOM_01", result.get(0).getRoomId());
        assertEquals(1200.5, result.get(0).getCurrentPrice());

        assertEquals("ROOM_02", result.get(1).getRoomId());
        assertEquals(50.0, result.get(1).getCurrentPrice());
    }

    @Test
    @DisplayName("Test bỏ qua các phòng bị thiếu thông tin (< 3 trường)")
    void testMap_IncompleteEntry_IgnoresEntry() {
        // Chuỗi này phòng số 2 bị thiếu giá tiền
        String rawData = "ROOM_01|Laptop Dell|1200.5;ROOM_02|Chuột Razer";
        List<AuctionRoom> result = mapper.map(rawData);

        assertEquals(1, result.size(), "Phòng số 2 bị thiếu dữ liệu nên phải bị loại bỏ");
        assertEquals("ROOM_01", result.get(0).getRoomId());
    }

    @Test
    @DisplayName("Test ném lỗi NumberFormatException khi giá tiền không phải là số")
    void testMap_InvalidPriceFormat_ThrowsException() {
        // Giá tiền bị nhập chữ "MộtNgàn" thay vì số
        String rawData = "ROOM_01|Laptop Dell|MộtNgàn";

        // Xác thực xem hàm có ném ra đúng lỗi như thiết kế không
        assertThrows(NumberFormatException.class, () -> {
            mapper.map(rawData);
        }, "Hệ thống phải ném lỗi NumberFormatException khi không parse được giá");
    }
}