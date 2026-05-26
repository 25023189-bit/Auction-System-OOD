package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.IAuctionDAO;
import com.auction.server.dao.IBidDAO;
import com.auction.server.dao.IUserDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuctionRoomServiceBranchTest {
    private static final String ROOM_ID = "BRANCH_ROOM";
    private IAuctionDAO auctionDAO;
    private IUserDAO userDAO;
    private IBidDAO bidDAO;
    private AuctionRoomService service;
    private AuctionRoom room;
    private User bidder;

    @BeforeEach
    void setUp() {
        auctionDAO = mock(IAuctionDAO.class);
        userDAO = mock(IUserDAO.class);
        bidDAO = mock(IBidDAO.class);
        service = new AuctionRoomService(auctionDAO, userDAO, bidDAO);
        room = validRoom();
        bidder = user("B1", "buyer", "BIDDER", 10000.0);
        when(auctionDAO.getAuctionById(ROOM_ID)).thenReturn(room);
        when(userDAO.getUserById("B1")).thenReturn(bidder);
    }

    @AfterEach
    void clearState() {
        AuctionStateManager.removeState(ROOM_ID);
    }

    @Test
    void joinRejectsUnknownUserScheduleBalanceAndClosedStatuses() {
        assertEquals("ROOM_FAIL", service.joinRoom(ROOM_ID, "missing").getAction());

        room.setEndTime(null);
        assertEquals("ROOM_FAIL", service.joinRoom(ROOM_ID, "B1").getAction());
        room.setEndTime(LocalDateTime.now().plusMinutes(10));
        room.setMinimumJoinAmount(11000.0);
        assertEquals("ROOM_FAIL", service.joinRoom(ROOM_ID, "B1").getAction());

        room.setMinimumJoinAmount(0.0);
        for (String status : new String[]{"SOLD", "UNSOLD", "ENDED", "CLOSED_BY_SELLER",
                "CANCELED", "CANCELED_BY_ADMIN", "unexpected"}) {
            room.setStatus(status);
            assertEquals("ROOM_FAIL", service.joinRoom(ROOM_ID, "B1").getAction());
        }
    }

    @Test
    void sellerCanEnterBeforeStartWhileBidderCannot() {
        room.setStartTime(LocalDateTime.now().plusMinutes(2));
        room.setSellerName("S1");
        assertEquals("ROOM_FAIL", service.joinRoom(ROOM_ID, "B1").getAction());

        User seller = user("S1", "seller", "SELLER", 0.0);
        when(userDAO.getUserById("S1")).thenReturn(seller);
        Message result = service.joinRoom(ROOM_ID, "S1");
        assertEquals("ROOM_JOINED", result.getAction());
    }

    @Test
    void bidRejectsIdentityMinimumStateAndDaoFailure() {
        assertEquals("BID_FAIL", service.placeNewBid(ROOM_ID, "unknown", 200.0).getAction());
        User seller = user("S1", "seller", "SELLER", 0.0);
        when(userDAO.getUserById("S1")).thenReturn(seller);
        assertEquals("BID_FAIL", service.placeNewBid(ROOM_ID, "S1", 200.0).getAction());

        assertEquals("BID_FAIL", service.placeNewBid(ROOM_ID, "B1", 105.0).getAction());
        assertEquals("BID_FAIL", service.placeNewBid(ROOM_ID, "B1", 120.0).getAction());

        assertEquals("ROOM_JOINED", service.joinRoom(ROOM_ID, "B1").getAction());
        when(bidDAO.placeBid(ROOM_ID, "B1", 120.0)).thenReturn(IBidDAO.BidResult.fail("rejected"));
        Message rejected = service.placeNewBid(ROOM_ID, "B1", 120.0);
        assertEquals("BID_FAIL", rejected.getAction());
        assertEquals("rejected", rejected.getData());
    }

    @Test
    void bidStatusValidationAndFinalWindowExtensionAreApplied() {
        assertEquals("ROOM_JOINED", service.joinRoom(ROOM_ID, "B1").getAction());
        for (String status : new String[]{"SOLD", "UNSOLD", "ENDED", "CLOSED_BY_SELLER",
                "CANCELED", "CANCELED_BY_ADMIN", "invalid"}) {
            room.setStatus(status);
            assertEquals("BID_FAIL", service.placeNewBid(ROOM_ID, "B1", 120.0).getAction());
        }

        room.setStatus("RUNNING");
        room.setStartTime(LocalDateTime.now().plusMinutes(1));
        assertEquals("BID_FAIL", service.placeNewBid(ROOM_ID, "B1", 120.0).getAction());

        room.setStartTime(LocalDateTime.now().minusMinutes(1));
        room.setEndTime(LocalDateTime.now().plusSeconds(20));
        room.setExtensionSeconds(60);
        when(bidDAO.placeBid(ROOM_ID, "B1", 120.0)).thenReturn(IBidDAO.BidResult.success());
        Message extended = service.placeNewBid(ROOM_ID, "B1", 120.0);
        assertEquals("BID_SUCCESS_EXTENDED", extended.getAction());
        assertEquals(120.0, room.getCurrentPrice());
        assertTrue(room.getExtendedSeconds() >= 60);
    }

    @Test
    void liveRoomAndExpirationChecksHandleEachOutcome() {
        when(auctionDAO.getAuctionById("missing")).thenReturn(null);
        assertNull(service.getLiveRoom("missing"));
        assertEquals(room, service.getLiveRoom(ROOM_ID));

        assertFalse(service.finalizeExpiredAuctionIfNeeded("missing"));
        room.setStatus("SOLD");
        assertFalse(service.finalizeExpiredAuctionIfNeeded(ROOM_ID));
        room.setStatus("RUNNING");
        room.setEndTime(null);
        assertFalse(service.finalizeExpiredAuctionIfNeeded(ROOM_ID));
        room.setEndTime(LocalDateTime.now().plusMinutes(1));
        assertFalse(service.finalizeExpiredAuctionIfNeeded(ROOM_ID));

        room.setEndTime(LocalDateTime.now().minusSeconds(1));
        when(auctionDAO.closeAuctionByTime(ROOM_ID)).thenReturn(IAuctionDAO.CloseAuctionResult.unsold());
        when(auctionDAO.getAllActiveAuctions()).thenReturn(Collections.emptyList());
        assertTrue(service.finalizeExpiredAuctionIfNeeded(ROOM_ID));
        verify(auctionDAO).closeAuctionByTime(ROOM_ID);
    }

    @Test
    void expirationSoldResultResolvesNamesAndBalances() {
        room.setEndTime(LocalDateTime.now().minusSeconds(1));
        User seller = user("S1", "seller", "SELLER", 0.0);
        when(userDAO.getUserById("S1")).thenReturn(seller);
        when(auctionDAO.closeAuctionByTime(ROOM_ID))
                .thenReturn(IAuctionDAO.CloseAuctionResult.sold("B1", "S1", 150.0, 850.0, 150.0));
        when(auctionDAO.getAllActiveAuctions()).thenReturn(Collections.emptyList());

        assertTrue(service.finalizeExpiredAuctionIfNeeded(ROOM_ID));
        verify(userDAO).getUserById("B1");
        verify(userDAO).getUserById("S1");
    }

    private AuctionRoom validRoom() {
        AuctionRoom newRoom = new AuctionRoom();
        newRoom.setRoomId(ROOM_ID);
        newRoom.setStatus("RUNNING");
        newRoom.setCurrentPrice(100.0);
        newRoom.setBidStep(10.0);
        newRoom.setStartTime(LocalDateTime.now().minusMinutes(1));
        newRoom.setEndTime(LocalDateTime.now().plusMinutes(10));
        return newRoom;
    }

    private User user(String id, String username, String role, double balance) {
        User user = new User();
        user.setCustomerId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setBalance(balance);
        return user;
    }
}
