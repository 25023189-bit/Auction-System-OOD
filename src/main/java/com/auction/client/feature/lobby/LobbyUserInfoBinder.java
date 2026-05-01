package com.auction.client.feature.lobby;

import com.auction.common.role.RolePolicy;
import com.auction.client.core.ui.ViewStateBinder;
import com.auction.common.model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Binder đưa thông tin user hiện tại lên header lobby.
 *
 * Vai trò:
 * - Hiển thị username và số dư ví của user đang đăng nhập.
 * - Bật/tắt nút tạo phiên đấu giá dựa trên RolePolicy.
 *
 * Luồng chính:
 * 1. Sau login hoặc quay về lobby, controller/handler gọi bind(user).
 * 2. Binder cập nhật label và trạng thái visible/managed của nút create auction.
 *
 * Business rules:
 * - Chỉ user có quyền canCreateAuction mới thấy nút tạo phiên.
 * - User null được bỏ qua để tránh bind dữ liệu không hợp lệ.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: control JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: ViewStateBinder<User>, Label, Button, RolePolicy.
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
