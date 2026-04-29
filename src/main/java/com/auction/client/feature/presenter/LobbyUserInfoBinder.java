package com.auction.client.feature.presenter;

import com.auction.common.model.User;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

/**
 * Binds user information to UI components in lobby
 */
public class LobbyUserInfoBinder {
    private Label lblUsername;
    private Label lblBalance;
    private Button btnCreateAuction;

    public LobbyUserInfoBinder() {
    }

    public void bind(Label lblUsername, Label lblBalance, Button btnCreateAuction) {
        this.lblUsername = lblUsername;
        this.lblBalance = lblBalance;
        this.btnCreateAuction = btnCreateAuction;
    }

    public void bindUserInfo(User user) {
        if (user == null) return;

        if (lblUsername != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : user.getId();
            lblUsername.setText(displayName);
        }

        if (lblBalance != null) {
            lblBalance.setText(String.format("Balance: $%.2f", user.getBalance()));
        }

        if (btnCreateAuction != null) {
            boolean isVisible = "SELLER".equalsIgnoreCase(user.getRole());
            btnCreateAuction.setVisible(isVisible);
            btnCreateAuction.setManaged(isVisible);
        }
    }

    public void updateBalance(double newBalance) {
        if (lblBalance != null) {
            lblBalance.setText(String.format("Balance: $%.2f", newBalance));
        }
    }
}
