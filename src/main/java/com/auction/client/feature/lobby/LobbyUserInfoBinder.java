package com.auction.client.feature.lobby;

import com.auction.common.role.RolePolicy;
import com.auction.client.core.ui.ViewStateBinder;
import com.auction.common.model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Bind thông tin user hiện tại lên header lobby.
 * Đồng thời quyết định seller có được thấy nút tạo phiên đấu giá hay không.
 */
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
            lblUsername.setText("Welcome: " + user.getUsername());
        }

        if (lblBalance != null) {
            lblBalance.setText("Wallet Balance: " + String.format("%,.0f $", user.getBalance()));
        }

        if (btnCreateAuction != null) {
            // Quyền tạo phiên không hard-code theo chuỗi role mà đi qua RolePolicy.
            boolean canCreate = rolePolicy.canCreateAuction(user);
            btnCreateAuction.setVisible(canCreate);
            btnCreateAuction.setManaged(canCreate);
        }
    }
}
