package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.handler.ClientActionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AutoBidManagerTest {

    private AutoBidManager autoBidManager;
    private ClientActionContext mockContext;
    private AuctionRoomService mockRoomService;
    private AuctionRoom testRoom;

    @BeforeEach
    void setUp() {
        autoBidManager = new AutoBidManager();
        mockContext = Mockito.mock(ClientActionContext.class);
        mockRoomService = Mockito.mock(AuctionRoomService.class);

        // Chuẩn bị dữ liệu phòng đấu giá ban đầu
        testRoom = new AuctionRoom();
        testRoom.setRoomId("ROOM_101");
        testRoom.setCurrentPrice(1000.0);
        testRoom.setHighestBidder("nguoi_ban_dau");

        // 1. Dạy Mockito: Khi hệ thống cần RoomService, hãy đưa bản Mock ra
        when(mockContext.getRoomService()).thenReturn(mockRoomService);

        // 2. TUYỆT KỸ THEN_ANSWER: Giả lập Database cập nhật giá tiền theo thời gian thực
        when(mockRoomService.placeNewBid(anyString(), anyString(), anyDouble()))
                .thenAnswer(invocation -> {
                    // Lấy các tham số mà Robot truyền vào
                    String userId = invocation.getArgument(1);
                    double amount = invocation.getArgument(2);

                    // Cập nhật trực tiếp vào testRoom để giả lập việc Database đã ghi nhận
                    testRoom.setCurrentPrice(amount);
                    testRoom.setHighestBidder(userId);

                    // Trả về Message thành công y hệt như Server thật
                    return new Message("BID_SUCCESS", "SERVER", testRoom);
                });
    }

    @Test
    @DisplayName("Kiểm tra đăng ký và hủy Robot thành công")
    void testRegisterAndCancelAutoBid() {
        autoBidManager.registerAutoBid("ROOM_101", "to_han", 5000, 100);

        autoBidManager.runAutoBiddingEngine(testRoom, mockContext);
        assertEquals("to_han", testRoom.getHighestBidder(), "Robot phải nhảy vào chiếm giá");

        autoBidManager.cancelAutoBid("ROOM_101", "to_han");

        // Giả lập có người khác vào mua
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

        // Giá gốc 1000 + Bước nhảy 100
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

        // Cuộc chiến diễn ra như sau:
        // 1. han nhảy vào: 1000 + 100 = 1100
        // 2. hancute nhảy vào: 1100 + 200 = 1300
        // 3. han bật lại: 1300 + 100 = 1400
        // 4. hancute muốn bật lại (1400 + 200 = 1600) -> NHƯNG vượt Max Bid (1500) -> Bỏ cuộc
        // KẾT QUẢ: han thắng ở mức 1400.

        assertEquals(1400.0, testRoom.getCurrentPrice(), "Giá cuối cùng phải chốt ở mức 1400");
        assertEquals("han", testRoom.getHighestBidder(), "Người chiến thắng phải là han");
    }
}