package com.auction.server.service;

import com.auction.common.model.AuctionRoom;
import com.auction.server.handler.ClientActionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AutoBidManagerTest {

    private AutoBidManager autoBidManager;
    private ClientActionContext mockContext;
    private AuctionRoom testRoom;

    @BeforeEach
    void setUp() {
        autoBidManager = new AutoBidManager();
        mockContext = Mockito.mock(ClientActionContext.class);

        testRoom = new AuctionRoom();
        testRoom.setRoomId("ROOM_101"); // Đã sửa theo đúng hàm của nhóm
        testRoom.setCurrentPrice(1000.0);
        testRoom.setHighestBidder("nguoi_ban_dau");
    }

    @Test
    @DisplayName("Kiểm tra đăng ký và hủy Robot thành công")
    void testRegisterAndCancelAutoBid() {
        autoBidManager.registerAutoBid("ROOM_101", "to_han", 5000, 100);

        autoBidManager.runAutoBiddingEngine(testRoom, mockContext);
        assertEquals("to_han", testRoom.getHighestBidder(), "Robot phải nhảy vào chiếm giá");

        autoBidManager.cancelAutoBid("ROOM_101", "to_han");
        testRoom.setCurrentPrice(2000.0);
        testRoom.setHighestBidder("nguoi_khac");

        autoBidManager.runAutoBiddingEngine(testRoom, mockContext);
        assertEquals(2000.0, testRoom.getCurrentPrice(), "Giá phải giữ nguyên vì Robot đã bị hủy");
    }

    @Test
    @DisplayName("Robot đơn độc tự nâng giá khi bị hụt")
    void testSingleAgentBidsSuccessfully() {
        autoBidManager.registerAutoBid("ROOM_101", "to_han", 5000.0, 100.0);
        autoBidManager.runAutoBiddingEngine(testRoom, mockContext);

        assertEquals(1100.0, testRoom.getCurrentPrice());
        assertEquals("to_han", testRoom.getHighestBidder());
        // Kiểm tra xem hệ thống có gọi lệnh gửi tin nhắn mạng (broadcast) không
        Mockito.verify(mockContext, Mockito.atLeastOnce()).broadcastToRoom(eq("ROOM_101"), any());
    }

    @Test
    @DisplayName("Hai Robot đọ giá nhau đến khi chạm trần")
    void testTwoAgentsBiddingWar() {
        // han cài max 2000, bước 100. hancute cài max 1500, bước 200
        autoBidManager.registerAutoBid("ROOM_101", "han", 2000.0, 100.0);
        autoBidManager.registerAutoBid("ROOM_101", "hancute", 1500.0, 200.0);

        autoBidManager.runAutoBiddingEngine(testRoom, mockContext);

        // Cuộc chiến dừng ở 1400 vì ở lượt tiếp theo hancute cần 1600 (vượt quá max 1500)
        assertEquals(1400.0, testRoom.getCurrentPrice(), "Giá cuối cùng phải chốt ở mức 1400");
        assertEquals("han", testRoom.getHighestBidder(), "Người chiến thắng phải là han");
    }
}