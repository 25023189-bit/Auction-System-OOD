package com.auction.client.feature.controllers.auction.room.lifecycle;

import com.auction.client.feature.room.AuctionCloseHandler;
import com.auction.client.session.SessionStore;
import javafx.scene.control.Button;

public class CloseAuctionController {
    private final AuctionCloseHandler closeHandler;
    private final SessionStore sessionStore;
    private final Button btnCloseAuction;

    public CloseAuctionController(AuctionCloseHandler closeHandler, SessionStore sessionStore, Button btnCloseAuction) {
        this.closeHandler = closeHandler;
        this.sessionStore = sessionStore;
        this.btnCloseAuction = btnCloseAuction;
    }

    public void handleCloseAuction() {
        if (closeHandler != null) {
            closeHandler.closeRoom(sessionStore != null ? sessionStore.getCurrentRoomId() : null);
        }
    }

    public void updateButtonVisibility() {
        if (btnCloseAuction == null || sessionStore == null) {
            return;
        }

        boolean visible = false;
        if (sessionStore.getCurrentUser() != null && sessionStore.getCurrentRoom() != null) {
            String currentUserId = sessionStore.getCurrentUser().getId();
            String role = sessionStore.getCurrentUser().getRole();
            String sellerIdOfRoom = sessionStore.getCurrentRoom().getSellerName();
            visible = "SELLER".equalsIgnoreCase(role)
                    && currentUserId != null
                    && currentUserId.equalsIgnoreCase(sellerIdOfRoom);
        }

        btnCloseAuction.setVisible(visible);
        btnCloseAuction.setManaged(visible);
    }
}
