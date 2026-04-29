package com.auction.client.feature.room;

import com.auction.client.core.ui.ViewPresenter;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Presenter cho màn hình phòng đấu giá.
 * Lớp này chỉ cập nhật control JavaFX, không gọi server và không giữ nghiệp vụ đấu giá.
 */
public class AuctionRoomPresenter implements ViewPresenter {
    private final Label lblAuctionItemName;
    private final Label lblCurrentPrice;
    private final Label lblTimer;
    private final Label lblParticipantCount;
    private final Label lblDescription;
    private final TextArea txtChatLog;
    private final TextArea txtItemDescriptionDisplay;
    private final Button btnCloseAuction;
    private final Button btnPlaceBid;
    private final TextField txtBidAmount;

    public AuctionRoomPresenter(Label lblAuctionItemName,
                                Label lblCurrentPrice,
                                Label lblTimer,
                                Label lblParticipantCount,
                                Label lblDescription,
                                TextArea txtChatLog,
                                TextArea txtItemDescriptionDisplay,
                                Button btnCloseAuction,
                                Button btnPlaceBid,
                                TextField txtBidAmount) {
        this.lblAuctionItemName = lblAuctionItemName;
        this.lblCurrentPrice = lblCurrentPrice;
        this.lblTimer = lblTimer;
        this.lblParticipantCount = lblParticipantCount;
        this.lblDescription = lblDescription;
        this.txtChatLog = txtChatLog;
        this.txtItemDescriptionDisplay = txtItemDescriptionDisplay;
        this.btnCloseAuction = btnCloseAuction;
        this.btnPlaceBid = btnPlaceBid;
        this.txtBidAmount = txtBidAmount;
    }

    // Hiển thị thông tin cơ bản của phòng: tên sản phẩm, giá hiện tại và mô tả.
    public void showRoomInfo(String itemName, double currentPrice, String description) {
        if (lblAuctionItemName != null) {
            lblAuctionItemName.setText(itemName);
        }
        if (lblCurrentPrice != null) {
            lblCurrentPrice.setText("Current Price: " + String.format("%,.0f $", currentPrice));
        }
        String resolvedDescription = (description != null && !description.isBlank())
                ? description
                : "No item description available.";
        if (txtItemDescriptionDisplay != null) {
            txtItemDescriptionDisplay.setText(resolvedDescription);
        } else if (lblDescription != null) {
            lblDescription.setText(resolvedDescription);
        }
    }

    public void showParticipantCount(int participantCount) {
        if (lblParticipantCount != null) {
            lblParticipantCount.setText("Participants: " + Math.max(participantCount, 0));
        }
    }

    public void appendChat(String line) {
        if (txtChatLog != null) {
            txtChatLog.appendText(line + "\n");
        }
    }

    // Cập nhật giá hiện tại; nếu có holderName thì ghi thêm vào chat log.
    public void showCurrentPrice(double price, String holderName) {
        if (lblCurrentPrice != null) {
            lblCurrentPrice.setText("Current Price: " + String.format("%,.0f $", price));
        }
        if (holderName != null && txtChatLog != null) {
            txtChatLog.appendText("New bid: " + holderName + " is holding the price at " + String.format("%,.0f $", price) + "\n");
        }
    }

    // Nút đóng phiên chỉ hiện với seller sở hữu phòng.
    public void setOwnerControlsVisible(boolean visible) {
        if (btnCloseAuction != null) {
            btnCloseAuction.setVisible(visible);
            btnCloseAuction.setManaged(visible);
        }
    }

    // Khóa nhập bid khi phiên chưa bắt đầu, đã kết thúc hoặc user không được bid.
    public void disableBidUi(String reason) {
        if (btnPlaceBid != null) {
            btnPlaceBid.setDisable(true);
        }
        if (txtBidAmount != null) {
            txtBidAmount.setDisable(true);
        }
        if (reason != null && txtChatLog != null) {
            txtChatLog.appendText(reason + "\n");
        }
    }

    // Timer đổi cả text và màu để người dùng nhận biết trạng thái thời gian.
    public void setTimerText(String text, Color color) {
        if (lblTimer != null) {
            lblTimer.setText(text);
            lblTimer.setTextFill(color);
        }
    }

    @Override
    public void clear() {
        if (txtChatLog != null) {
            txtChatLog.clear();
        }
    }
}
