package com.auction.client.feature.presenter;

import com.auction.common.model.User;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

/**
 * Binder thông tin user cho lobby legacy.
 *
 * Vai trò:
 * - Gắn các control header lobby qua bind().
 * - Hiển thị username, số dư và quyền hiện nút tạo phiên theo role.
 *
 * Luồng chính:
 * 1. Controller legacy gọi bind(labelUsername, labelBalance, btnCreateAuction).
 * 2. Khi có user hoặc balance mới, caller gọi bindUserInfo() hoặc updateBalance().
 *
 * Business rules:
 * - Chỉ SELLER được thấy nút tạo phiên đấu giá.
 * - Nếu username null thì fallback sang user id.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: control JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: User, Label, Button.
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
            // Chỉ seller được thấy nút tạo phiên đấu giá.
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
