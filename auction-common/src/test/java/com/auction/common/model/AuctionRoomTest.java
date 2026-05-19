package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionRoomTest {

    @Test
    @DisplayName("Test các Constructor khởi tạo")
    void testConstructors() {
        // Test Constructor 3 tham số
        AuctionRoom room1 = new AuctionRoom("R001", "MacBook", 1000.0);
        assertEquals("R001", room1.getRoomId());
        assertEquals("MacBook", room1.getItemName());
        assertEquals(1000.0, room1.getCurrentPrice());
        assertEquals(1000.0, room1.getStartingPrice());

        // Test Constructor 4 tham số
        AuctionRoom room2 = new AuctionRoom("R002", "iPhone", 500.0, "SellerA");
        assertEquals("R002", room2.getRoomId());
        assertEquals("iPhone", room2.getItemName());
        assertEquals(500.0, room2.getCurrentPrice());
        assertEquals(500.0, room2.getStartingPrice());
        assertEquals("SellerA", room2.getSellerName());
    }

    @Test
    @DisplayName("Test toàn bộ Getter, Setter tiêu chuẩn")
    void testStandardGettersAndSetters() {
        AuctionRoom room = new AuctionRoom();
        LocalDateTime time = LocalDateTime.now();

        room.setRoomId("R123");
        room.setItemId("I123");
        room.setItemName("iPad");
        room.setCurrentPrice(300.0);
        room.setStartingPrice(250.0);
        room.setBidStep(50.0);
        room.setMinimumJoinAmount(100.0);
        room.setSellerName("HanTo");
        room.setHighestBidder("UserX");
        room.setParticipantCount(10);
        room.setSellerReputation(4.5);
        room.setSellerSuccessfulAuctionRate(0.9);
        room.setSellerAdminCancellationRate(0.1);
        room.setStartTime(time);
        room.setEndTime(time.plusHours(1));
        room.setDurationMinutes(60);
        room.setExtensionSeconds(30);
        room.setStatus("ACTIVE");
        room.setExtendedSeconds(120L);
        room.setEntryLocked(true);
        room.setScheduledEndTime(time.plusHours(2));

        assertEquals("R123", room.getRoomId());
        assertEquals("I123", room.getItemId());
        assertEquals("iPad", room.getItemName());
        assertEquals(300.0, room.getCurrentPrice());
        assertEquals(250.0, room.getStartingPrice());
        assertEquals(50.0, room.getBidStep());
        assertEquals(100.0, room.getMinimumJoinAmount());
        assertEquals("HanTo", room.getSellerName());
        assertEquals("UserX", room.getHighestBidder());
        assertEquals(10, room.getParticipantCount());
        assertEquals(4.5, room.getSellerReputation());
        assertEquals(0.9, room.getSellerSuccessfulAuctionRate());
        assertEquals(0.1, room.getSellerAdminCancellationRate());
        assertEquals(time, room.getStartTime());
        assertEquals(time.plusHours(1), room.getEndTime());
        assertEquals(60, room.getDurationMinutes());
        assertEquals(30, room.getExtensionSeconds());
        assertEquals("ACTIVE", room.getStatus());
        assertEquals(120L, room.getExtendedSeconds());
        assertTrue(room.isEntryLocked());
        assertEquals(time.plusHours(2), room.getScheduledEndTime());
    }

    @Test
    @DisplayName("Test các hàm Alias (Bí danh) gọi chéo nhau")
    void testAliasMethods() {
        AuctionRoom room = new AuctionRoom();
        LocalDateTime time = LocalDateTime.now();

        // setAuctionId thay đổi roomId
        room.setAuctionId("A001");
        assertEquals("A001", room.getAuctionId());
        assertEquals("A001", room.getRoomId());

        // setProductId thay đổi itemId
        room.setProductId("P001");
        assertEquals("P001", room.getProductId());
        assertEquals("P001", room.getItemId());

        // getNameSeller lấy từ sellerName
        room.setSellerName("AliasSeller");
        assertEquals("AliasSeller", room.getNameSeller());

        // setActualEndTime thay đổi endTime
        room.setActualEndTime(time);
        assertEquals(time, room.getActualEndTime());
        assertEquals(time, room.getEndTime());
    }

    @Test
    @DisplayName("Test logic mặc định của ItemDescription")
    void testItemDescriptionLogic() {
        AuctionRoom room = new AuctionRoom();

        // 1. Nếu null -> Trả về "Khong co mo ta"
        room.setItemDescription(null);
        assertEquals("Khong co mo ta", room.getItemDescription());

        // 2. Nếu chuỗi rỗng/khoảng trắng -> Trả về "Khong co mo ta"
        room.setItemDescription("   ");
        assertEquals("Khong co mo ta", room.getItemDescription());

        // 3. Nếu có giá trị hợp lệ -> Trả về chính giá trị đó
        room.setItemDescription("Hang cuc chat");
        assertEquals("Hang cuc chat", room.getItemDescription());
    }
}