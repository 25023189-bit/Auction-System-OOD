package com.auction.server.handler;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PendingAuctionRoomFactoryTest {

    @Test
    @DisplayName("Test tạo AuctionRoom và Item từ Request (Trạng thái OPEN)")
    void testCreateRoomAndItem_OpenStatus() {
        PendingAuctionRoomFactory factory = new PendingAuctionRoomFactory();

        // Cố tình đặt startTime ở tương lai (+1 ngày) để kích hoạt trạng thái "OPEN"
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);

        PendingAuctionRequest request = new PendingAuctionRequest(
                "REQ_01", "ROOM_01", "ITEM_01", "SELLER_01", "UET_ORG",
                "Tranh Sơn Dầu", "Mô tả tranh", 1000.0, 100.0, 50.0,
                futureStart, 60, 30, 4.8, 0.9, 0.02
        );

        // 1. Kiểm tra việc tạo Room
        AuctionRoom room = factory.createRoom(request);
        assertNotNull(room);
        assertEquals("ROOM_01", room.getRoomId());
        assertEquals("Tranh Sơn Dầu", room.getItemName());
        assertEquals(1000.0, room.getStartingPrice());
        assertEquals("OPEN", room.getStatus(), "Thời gian ở tương lai thì status phải là OPEN");
        assertEquals(futureStart.plusMinutes(60), room.getActualEndTime());

        // 2. Kiểm tra việc tạo Item
        Item item = factory.createItem(request);
        assertNotNull(item);
        assertEquals("ITEM_01", item.getId());
        assertEquals("Tranh Sơn Dầu", item.getName());
        assertEquals(1000.0, item.getStartingPrice());
    }

    @Test
    @DisplayName("Test tạo AuctionRoom với thời gian đã qua (Trạng thái RUNNING)")
    void testCreateRoom_RunningStatus() {
        PendingAuctionRoomFactory factory = new PendingAuctionRoomFactory();

        // Cố tình đặt startTime ở quá khứ (-1 tiếng) để kích hoạt trạng thái "RUNNING"
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);

        PendingAuctionRequest request = new PendingAuctionRequest(
                "REQ_02", "ROOM_02", "ITEM_02", "SELLER_02", "UET_ORG",
                "Đồ cổ", "Mô tả đồ cổ", 500.0, 50.0, 10.0,
                pastStart, 30, 10, 4.5, 0.8, 0.05
        );

        AuctionRoom room = factory.createRoom(request);
        assertNotNull(room);
        assertEquals("RUNNING", room.getStatus(), "Thời gian ở quá khứ/hiện tại thì status phải là RUNNING");
    }
}