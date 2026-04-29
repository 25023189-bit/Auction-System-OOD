package com.auction.client.feature.presenter;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

/**
 * Presenter phòng đấu giá phiên bản nhẹ, gom các thao tác cập nhật control JavaFX.
 */
public class AuctionRoomPresenter {
    private Label lblItemName;
    private Label lblCurrentPrice;
    private Label lblTimer;
    private Label lblParticipantCount;
    private Label lblDescription;
    private TextArea txtChatLog;
    private TextArea txtItemDescription;
    private Button btnCloseAuction;
    private Button btnPlaceBid;
    private TextField txtBidAmount;

    public AuctionRoomPresenter(Label lblItemName, Label lblCurrentPrice, Label lblTimer,
                                Label lblParticipantCount, Label lblDescription,
                                TextArea txtChatLog, TextArea txtItemDescription,
                                Button btnCloseAuction, Button btnPlaceBid,
                                TextField txtBidAmount) {
        this.lblItemName = lblItemName;
        this.lblCurrentPrice = lblCurrentPrice;
        this.lblTimer = lblTimer;
        this.lblParticipantCount = lblParticipantCount;
        this.lblDescription = lblDescription;
        this.txtChatLog = txtChatLog;
        this.txtItemDescription = txtItemDescription;
        this.btnCloseAuction = btnCloseAuction;
        this.btnPlaceBid = btnPlaceBid;
        this.txtBidAmount = txtBidAmount;
    }

    public void updateItemName(String name) {
        if (lblItemName != null) lblItemName.setText(name);
    }

    public void updateCurrentPrice(String price) {
        if (lblCurrentPrice != null) lblCurrentPrice.setText(price);
    }

    public void updateTimer(String time) {
        if (lblTimer != null) lblTimer.setText(time);
    }

    public void updateParticipantCount(String count) {
        if (lblParticipantCount != null) lblParticipantCount.setText(count);
    }

    public void updateDescription(String desc) {
        if (lblDescription != null) lblDescription.setText(desc);
    }

    public void addChatMessage(String message) {
        // appendText giữ lại lịch sử chat thay vì thay thế toàn bộ nội dung.
        if (txtChatLog != null) {
            txtChatLog.appendText(message + "\n");
        }
    }

    public void clearChat() {
        if (txtChatLog != null) txtChatLog.clear();
    }

    public String getBidAmount() {
        return txtBidAmount != null ? txtBidAmount.getText() : "";
    }

    public void clearBidAmount() {
        if (txtBidAmount != null) txtBidAmount.clear();
    }
}
