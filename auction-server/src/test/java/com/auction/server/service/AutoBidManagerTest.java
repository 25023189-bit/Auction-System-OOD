package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.handler.ClientActionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        testRoom = new AuctionRoom();
        testRoom.setRoomId("ROOM_101");
        testRoom.setCurrentPrice(1000.0);
        testRoom.setBidStep(100.0);
        testRoom.setHighestBidder("USER_B");

        when(mockContext.getRoomService()).thenReturn(mockRoomService);
        when(mockRoomService.placeNewBid(anyString(), anyString(), anyDouble()))
                .thenAnswer(invocation -> {
                    String userId = invocation.getArgument(1);
                    double amount = invocation.getArgument(2);
                    testRoom.setCurrentPrice(amount);
                    testRoom.setHighestBidder(userId);
                    return new Message("BID_SUCCESS", "SERVER", testRoom);
                });
    }

    @Test
    void registerAutoBidStoresAgentByUserId() {
        autoBidManager.registerAutoBid("ROOM_101", "USER_A", 2000, 100);

        assertTrue(autoBidManager.hasAutoBid("ROOM_101", "USER_A"));
        assertFalse(autoBidManager.hasAutoBid("ROOM_101", "username_a"));
    }

    @Test
    void cancelAutoBidUsesUserId() {
        autoBidManager.registerAutoBid("ROOM_101", "USER_A", 2000, 100);

        assertFalse(autoBidManager.cancelAutoBid("ROOM_101", "username_a"));
        assertTrue(autoBidManager.hasAutoBid("ROOM_101", "USER_A"));
        assertTrue(autoBidManager.cancelAutoBid("ROOM_101", "USER_A"));
        assertFalse(autoBidManager.hasAutoBid("ROOM_101", "USER_A"));
    }

    @Test
    void triggerDoesNotBidForCurrentHighestBidder() {
        testRoom.setHighestBidder("USER_A");
        autoBidManager.registerAutoBid("ROOM_101", "USER_A", 2000, 100);

        autoBidManager.triggerAutoBids("ROOM_101", testRoom, mockContext);

        assertEquals(1000.0, testRoom.getCurrentPrice());
        verify(mockRoomService, never()).placeNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    void triggerBidsWhenAgentIsNotHighestBidder() {
        autoBidManager.registerAutoBid("ROOM_101", "USER_A", 1500, 100);

        autoBidManager.triggerAutoBids("ROOM_101", testRoom, mockContext);

        assertEquals(1100.0, testRoom.getCurrentPrice());
        assertEquals("USER_A", testRoom.getHighestBidder());
        verify(mockRoomService).placeNewBid(eq("ROOM_101"), eq("USER_A"), eq(1100.0));
    }

    @Test
    void triggerDoesNotExceedMaxBid() {
        autoBidManager.registerAutoBid("ROOM_101", "USER_A", 1100, 200);

        autoBidManager.triggerAutoBids("ROOM_101", testRoom, mockContext);

        assertEquals(1000.0, testRoom.getCurrentPrice());
        assertEquals("USER_B", testRoom.getHighestBidder());
        verify(mockRoomService, never()).placeNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    void twoAgentsBidDeterministicallyUntilStable() {
        autoBidManager.registerAutoBid("ROOM_101", "USER_A", 2000.0, 100.0);
        autoBidManager.registerAutoBid("ROOM_101", "USER_C", 1500.0, 200.0);

        autoBidManager.triggerAutoBids("ROOM_101", testRoom, mockContext);

        assertEquals(1400.0, testRoom.getCurrentPrice());
        assertEquals("USER_A", testRoom.getHighestBidder());
    }
}
