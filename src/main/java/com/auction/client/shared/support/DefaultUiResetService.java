package com.auction.client.shared.support;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Service reset các control UI phụ thuộc session về trạng thái sạch.
 *
 * Vai trò:
 * - Xóa dữ liệu hiển thị của user/room cũ khi logout hoặc session bị vô hiệu.
 * - Khôi phục trạng thái enable/visible mặc định cho nút bid, tạo phiên và đóng phiên.
 *
 * Luồng chính:
 * 1. AuctionController tạo service với các control có trong FXML hiện tại.
 * 2. Khi logout/reset session, controller gọi resetSessionUi() để dọn UI.
 *
 * Business rules:
 * - Control theo role như create/close auction phải bị ẩn để user sau không thấy quyền của user trước.
 * - Dữ liệu chat, giá, timer và thông tin user cũ phải được xóa khỏi màn hình.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: thao tác control JavaFX phải chạy trên JavaFX Application Thread.
 * - Dependency: UiResetService, Button, TextField, TextArea, Label.
 */
public class DefaultUiResetService implements UiResetService {
    private final Button btnCreateAuction;
    private final Button btnCloseAuction;
    private final Button btnPlaceBid;
    private final TextField txtBidAmount;
    private final TextField txtChatInput;
    private final TextArea txtChatLog;
    private final Label lblUsername;
    private final Label lblBalance;
    private final Label lblAuctionItemName;
    private final Label lblCurrentPrice;
    private final Label lblTimer;

    public DefaultUiResetService(Button btnCreateAuction,
                                 Button btnCloseAuction,
                                 Button btnPlaceBid,
                                 TextField txtBidAmount,
                                 TextField txtChatInput,
                                 TextArea txtChatLog,
                                 Label lblUsername,
                                 Label lblBalance,
                                 Label lblAuctionItemName,
                                 Label lblCurrentPrice,
                                 Label lblTimer) {
        this.btnCreateAuction = btnCreateAuction;
        this.btnCloseAuction = btnCloseAuction;
        this.btnPlaceBid = btnPlaceBid;
        this.txtBidAmount = txtBidAmount;
        this.txtChatInput = txtChatInput;
        this.txtChatLog = txtChatLog;
        this.lblUsername = lblUsername;
        this.lblBalance = lblBalance;
        this.lblAuctionItemName = lblAuctionItemName;
        this.lblCurrentPrice = lblCurrentPrice;
        this.lblTimer = lblTimer;
    }

    @Override
    public void resetSessionUi() {
        // Ẩn các nút phụ thuộc role để user kế tiếp không thấy quyền của user cũ.
        if (btnCreateAuction != null) {
            btnCreateAuction.setVisible(false);
            btnCreateAuction.setManaged(false);
        }

        if (btnCloseAuction != null) {
            btnCloseAuction.setVisible(false);
            btnCloseAuction.setManaged(false);
        }

        if (btnPlaceBid != null) {
            btnPlaceBid.setDisable(false);
        }

        if (txtBidAmount != null) {
            txtBidAmount.clear();
            txtBidAmount.setDisable(false);
        }

        if (txtChatInput != null) {
            txtChatInput.clear();
            txtChatInput.setDisable(false);
        }

        // Xóa dữ liệu hiển thị thuộc phiên cũ.
        if (txtChatLog != null) txtChatLog.clear();
        if (lblUsername != null) lblUsername.setText("");
        if (lblBalance != null) lblBalance.setText("");
        if (lblAuctionItemName != null) lblAuctionItemName.setText("");
        if (lblCurrentPrice != null) lblCurrentPrice.setText("");
        if (lblTimer != null) lblTimer.setText("");
    }
}
