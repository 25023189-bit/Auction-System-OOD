package com.auction.client.feature.controllers.auction.room.lifecycle;

import com.auction.client.feature.room.AuctionCloseHandler;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloseAuctionControllerTest {
    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    void requestsCloseForCurrentRoomAndShowsButtonToItsSeller() {
        AuctionCloseHandler handler = mock(AuctionCloseHandler.class);
        SessionStore session = mock(SessionStore.class);
        Button closeButton = new Button();
        User seller = user("S1", "SELLER");
        AuctionRoom room = new AuctionRoom();
        room.setSellerName("S1");
        when(session.getCurrentRoomId()).thenReturn("R1");
        when(session.getCurrentUser()).thenReturn(seller);
        when(session.getCurrentRoom()).thenReturn(room);
        CloseAuctionController controller = new CloseAuctionController(handler, session, closeButton);

        controller.handleCloseAuction();
        controller.updateButtonVisibility();

        verify(handler).closeRoom("R1");
        assertTrue(closeButton.isVisible());
        assertTrue(closeButton.isManaged());
    }

    @Test
    void hidesButtonForBidderOrDifferentSellerAndToleratesMissingDependencies() {
        SessionStore session = mock(SessionStore.class);
        Button closeButton = new Button();
        AuctionRoom room = new AuctionRoom();
        room.setSellerName("S2");
        when(session.getCurrentUser()).thenReturn(user("B1", "BIDDER"));
        when(session.getCurrentRoom()).thenReturn(room);
        CloseAuctionController controller = new CloseAuctionController(null, session, closeButton);

        controller.handleCloseAuction();
        controller.updateButtonVisibility();
        assertFalse(closeButton.isVisible());
        assertFalse(closeButton.isManaged());

        new CloseAuctionController(null, null, null).updateButtonVisibility();
    }

    private User user(String id, String role) {
        User user = new User();
        user.setCustomerId(id);
        user.setRole(role);
        return user;
    }
}
