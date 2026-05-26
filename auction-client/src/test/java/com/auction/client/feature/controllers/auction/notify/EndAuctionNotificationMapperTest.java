package com.auction.client.feature.controllers.auction.notify;

import com.auction.common.dto.AuctionEndNotificationPayload;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndAuctionNotificationMapperTest {
    private final EndAuctionNotificationMapper mapper = new EndAuctionNotificationMapper();

    @Test
    void mapsWinningAndLosingBidderOutcomes() {
        AuctionEndNotificationPayload payload = payload();
        User winner = user("B1", "winning-user", "BIDDER");

        EndAuctionNotificationViewModel win = mapper.map(new Message("AUCTION_ENDED", "SERVER", payload), winner, null);

        assertEquals(EndAuctionResultType.BIDDER_WIN, win.getResultType());
        assertEquals("winning-user", win.getWinnerUsername());
        assertEquals(AuctionEndReason.TIME_EXPIRED, win.getEndReason());
        assertTrue(win.hasWinner());
        assertTrue(win.isTransactionApplied());

        payload.setWinnerUsername("other-user");
        User loser = user("B2", "losing-user", "BIDDER");
        EndAuctionNotificationViewModel lose = mapper.map(new Message("AUCTION_ENDED", "SERVER", payload), loser, null);

        assertEquals(EndAuctionResultType.BIDDER_LOSE, lose.getResultType());
        assertEquals("other-user", lose.getWinnerUsername());
    }

    @Test
    void mapsSellerAndAdministrativeOutcomes() {
        AuctionEndNotificationPayload soldPayload = payload();
        User seller = user("S1", "seller", "SELLER");
        assertEquals(EndAuctionResultType.SELLER_SOLD,
                mapper.map(new Message("AUCTION_ENDED", "SERVER", soldPayload), seller, null).getResultType());

        soldPayload.setHasWinner(false);
        assertEquals(EndAuctionResultType.SELLER_NO_WINNER,
                mapper.map(new Message("AUCTION_ENDED", "SERVER", soldPayload), seller, null).getResultType());

        soldPayload.setEndReason("SELLER_CLOSED");
        assertEquals(EndAuctionResultType.CLOSED_BY_SELLER,
                mapper.map(new Message("AUCTION_ENDED", "SERVER", soldPayload), seller, null).getResultType());

        soldPayload.setEndReason("ADMIN_BANNED");
        assertEquals(EndAuctionResultType.CLOSED_BY_ADMIN,
                mapper.map(new Message("AUCTION_ENDED", "SERVER", soldPayload), seller, null).getResultType());
    }

    @Test
    void fallsBackToRoomDataForLegacyOrMissingPayload() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("R2");
        room.setItemName("Vintage Watch");
        room.setCurrentPrice(1250.0);
        room.setHighestBidder("");
        room.setStatus("ENDED");

        EndAuctionNotificationViewModel legacy =
                mapper.map(new Message("AUCTION_ENDED", "SERVER", "R2"), null, room);

        assertEquals("R2", legacy.getAuctionId());
        assertEquals("Vintage Watch", legacy.getItemName());
        assertEquals(1250.0, legacy.getFinalPrice());
        assertEquals("Khong co nguoi thang", legacy.getWinnerUsername());
        assertEquals(EndAuctionResultType.NO_WINNER, legacy.getResultType());
        assertFalse(legacy.isTransactionApplied());

        EndAuctionNotificationViewModel missing = mapper.map(null, null, null);
        assertEquals("Chua co thong tin", missing.getAuctionId());
        assertEquals(AuctionEndReason.UNKNOWN, missing.getEndReason());
        assertEquals(EndAuctionResultType.NO_WINNER, missing.getResultType());
    }

    private AuctionEndNotificationPayload payload() {
        AuctionEndNotificationPayload payload = new AuctionEndNotificationPayload();
        payload.setAuctionId("R1");
        payload.setItemName("Camera");
        payload.setFinalPrice(500.0);
        payload.setWinnerId("B1");
        payload.setHasWinner(true);
        payload.setEndReason("TIME_EXPIRED");
        payload.setTransactionApplied(true);
        return payload;
    }

    private User user(String id, String username, String role) {
        User user = new User();
        user.setCustomerId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }
}
