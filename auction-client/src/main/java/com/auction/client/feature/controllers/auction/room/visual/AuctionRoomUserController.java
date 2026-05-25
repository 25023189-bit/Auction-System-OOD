package com.auction.client.feature.controllers.auction.room.visual;

import com.auction.common.model.User;
import javafx.scene.control.Label;

public class AuctionRoomUserController {
    private final Label lblUsername;

    public AuctionRoomUserController(Label lblUsername) {
        this.lblUsername = lblUsername;
    }

    public void bind(User user) {
        if (lblUsername == null || user == null) return;
        String displayName = user.getUsername() != null && !user.getUsername().isBlank() ? user.getUsername() : user.getId();
        String prefix = "SELLER".equalsIgnoreCase(user.getRole()) ? "Seller: " : "User: ";
        lblUsername.setText(prefix + displayName);
    }
}
