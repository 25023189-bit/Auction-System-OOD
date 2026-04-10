package com.auction.client.support;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

        if (txtChatLog != null) txtChatLog.clear();
        if (lblUsername != null) lblUsername.setText("");
        if (lblBalance != null) lblBalance.setText("");
        if (lblAuctionItemName != null) lblAuctionItemName.setText("");
        if (lblCurrentPrice != null) lblCurrentPrice.setText("");
        if (lblTimer != null) lblTimer.setText("");
    }
}