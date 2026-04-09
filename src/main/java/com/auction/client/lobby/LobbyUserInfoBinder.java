package com.auction.client.lobby;

import com.auction.client.role.RolePolicy;
import com.auction.client.ui.ViewStateBinder;
import com.auction.common.model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class LobbyUserInfoBinder implements ViewStateBinder<User> {
    private final Label lblUsername;
    private final Label lblBalance;
    private final Button btnCreateAuction;
    private final RolePolicy rolePolicy;

    public LobbyUserInfoBinder(Label lblUsername,
                               Label lblBalance,
                               Button btnCreateAuction,
                               RolePolicy rolePolicy) {
        this.lblUsername = lblUsername;
        this.lblBalance = lblBalance;
        this.btnCreateAuction = btnCreateAuction;
        this.rolePolicy = rolePolicy;
    }

    @Override
    public void bind(User user) {
        if (user == null) return;

        if (lblUsername != null) {
            lblUsername.setText("Xin chào: " + user.getUsername());
        }

        if (lblBalance != null) {
            lblBalance.setText("Số dư ví: " + String.format("%,.0f $", user.getBalance()));
        }

        if (btnCreateAuction != null) {
            boolean canCreate = rolePolicy.canCreateAuction(user);
            btnCreateAuction.setVisible(canCreate);
            btnCreateAuction.setManaged(canCreate);
        }
    }
}