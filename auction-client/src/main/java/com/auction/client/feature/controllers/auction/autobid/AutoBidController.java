package com.auction.client.feature.controllers.auction.autobid;

import com.auction.client.network.socket.ClientConnection;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AutoBidController {
    private final VBox paneAutoBid;
    private final TextArea txtChatLog;
    private final SessionStore sessionStore;
    private final ClientConnection clientConnection;
    private final AutoBidFormReader formReader;
    private final AutoBidValidator validator;
    private final AutoBidButtonStateController buttonStateController;
    private boolean autoBidActive;

    public AutoBidController(
            VBox paneAutoBid,
            TextField txtMaxBid,
            TextField txtAutoBidStep,
            Button btnToggleAutoBid,
            TextArea txtChatLog,
            SessionStore sessionStore,
            ClientConnection clientConnection
    ) {
        this.paneAutoBid = paneAutoBid;
        this.txtChatLog = txtChatLog;
        this.sessionStore = sessionStore; // <-- Vũ khí bí mật là đây
        this.clientConnection = clientConnection;
        this.formReader = new AutoBidFormReader(txtMaxBid, txtAutoBidStep);
        this.validator = new AutoBidValidator();
        this.buttonStateController = new AutoBidButtonStateController(btnToggleAutoBid);
    }

    public void togglePanel() {
        if (paneAutoBid == null) {
            return;
        }

        boolean shouldShow = !paneAutoBid.isVisible();
        paneAutoBid.setVisible(shouldShow);
        paneAutoBid.setManaged(shouldShow);
    }

    public void hidePanel() {
        if (paneAutoBid == null) {
            return;
        }
        paneAutoBid.setVisible(false);
        paneAutoBid.setManaged(false);
    }

    public void handleSetAutoBid() {
        // 1. TỰ ĐỘNG THÒ TAY VÀO KHO LẤY DỮ LIỆU PHÒNG MỚI NHẤT
        AuctionRoom latestRoom = sessionStore != null ? sessionStore.getCurrentRoom() : null;

        if (latestRoom == null) {
            appendFeedback("He thong: Loi! Chua cap nhat duoc thong tin phong dau gia.\n");
            return;
        }

        // 2. Đưa phòng mới nhất cho Validator kiểm tra luật
        AutoBidValidator.AutoBidValidationResult result = validator.validate(formReader.read(), latestRoom);

        if (!result.valid()) {
            appendFeedback(result.message());
            return;
        }

        appendFeedback(String.format(
                "He thong: Dang thiet lap Auto-Bid... Max: %.0f$, Buoc: %.0f$\n",
                result.maxBid(),
                result.step()
        ));
        buttonStateController.markPending();

        // 3. Gửi lệnh lên Server
        String roomId = latestRoom.getRoomId();
        if (roomId != null && clientConnection != null) {
            clientConnection.sendMessage(new Message("SET_AUTO_BID", new AutoBidRequest(roomId, result.maxBid(), result.step())));
        }
    }

    public void handleCancelAutoBid() {
        buttonStateController.markPending();
        appendFeedback("He thong: Dang huy Auto-Bid...\n");

        String roomId = currentRoomId();
        if (roomId != null && clientConnection != null) {
            clientConnection.sendMessage(new Message("CANCEL_AUTO_BID", roomId));
        }
    }

    public void handleSetSuccess() {
        autoBidActive = true;
        buttonStateController.markOn();
        hidePanel();
        appendFeedback("He thong: Da thiet lap Auto-Bid!\n");
    }

    public void handleSetFailed(String errorMessage) {
        autoBidActive = false;
        buttonStateController.markOff();
        appendFeedback("He thong: Khong the thiet lap Auto-Bid. " + safeMessage(errorMessage) + "\n");
    }

    public void handleCancelSuccess() {
        autoBidActive = false;
        buttonStateController.markOff();
        formReader.clear();
        hidePanel();
        appendFeedback("He thong: Da huy Auto-Bid!\n");
    }

    public void handleCancelFailed(String errorMessage) {
        if (autoBidActive) {
            buttonStateController.markOn();
        } else {
            buttonStateController.markOff();
        }
        appendFeedback("He thong: Khong the huy Auto-Bid. " + safeMessage(errorMessage) + "\n");
    }

    boolean isAutoBidActive() {
        return autoBidActive;
    }

    private String currentRoomId() {
        return sessionStore != null ? sessionStore.getCurrentRoomId() : null;
    }

    private void appendFeedback(String message) {
        if (txtChatLog != null && message != null) {
            txtChatLog.appendText(message);
        }
    }

    private String safeMessage(String message) {
        return message == null ? "" : message;
    }
}
