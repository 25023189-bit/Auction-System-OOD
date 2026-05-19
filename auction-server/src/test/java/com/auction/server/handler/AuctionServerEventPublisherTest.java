package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.main.AuctionServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class AuctionServerEventPublisherTest {

    private MockedStatic<AuctionServer> mockedServer;
    private AuctionServerEventPublisher publisher;

    @BeforeEach
    void setUp() {
        // Bắt cóc class static AuctionServer
        mockedServer = mockStatic(AuctionServer.class);
        publisher = new AuctionServerEventPublisher();
    }

    @AfterEach
    void tearDown() {
        // Giải thoát class static sau mỗi bài test
        mockedServer.close();
    }

    @Test
    @DisplayName("Test phát event toàn hệ thống (Broadcast All)")
    void testBroadcastAll() {
        Message msg = new Message("TEST_ALL", "DATA");
        publisher.broadcastAll(msg);

        // Kiểm tra xem hàm static của AuctionServer có thực sự được gọi không
        mockedServer.verify(() -> AuctionServer.broadcast(msg), times(1));
    }

    @Test
    @DisplayName("Test phát event riêng cho một phòng")
    void testBroadcastToRoom() {
        Message msg = new Message("TEST_ROOM", "DATA");
        publisher.broadcastToRoom("ROOM_001", msg);

        mockedServer.verify(() -> AuctionServer.broadcastToRoom("ROOM_001", msg), times(1));
    }

    @Test
    @DisplayName("Test thông báo đóng phòng đấu giá")
    void testNotifyRoomClosed() {
        publisher.notifyRoomClosed("ROOM_001");
        mockedServer.verify(() -> AuctionServer.notifyRoomClosed("ROOM_001"), times(1));
    }

    @Test
    @DisplayName("Test thông báo xóa/khóa tài khoản user")
    void testNotifyDeletedUser() {
        publisher.notifyDeletedUser("USER_01");
        mockedServer.verify(() -> AuctionServer.notifyDeletedUser("USER_01"), times(1));
    }
}