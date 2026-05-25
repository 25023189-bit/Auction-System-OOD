package com.auction.client.feature.controllers.auction.notify;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class NotifyEndAuctionController {
    @FXML private Label lblStatusIcon;
    @FXML private Label lblTitle;
    @FXML private Label lblBody;
    @FXML private Label lblAuctionId;
    @FXML private Label lblItemName;
    @FXML private Label lblFinalPrice;
    @FXML private Label lblWinner;
    @FXML private Label lblEndReason;
    @FXML private Label lblTransaction;
    @FXML private Label lblNextAction;
    @FXML private Button btnBackToLobby;
    @FXML private Button btnSellerDashboard;
    @FXML private Button btnViewOtherAuctions;

    private Runnable backToLobbyAction;
    private Runnable sellerDashboardAction;
    private Runnable viewOtherAuctionsAction;

    @FXML
    private void initialize() {
        bindAction(btnBackToLobby, this::backToLobby);
        bindAction(btnSellerDashboard, this::openSellerDashboard);
        bindAction(btnViewOtherAuctions, this::viewOtherAuctions);
    }

    public void setNotificationData(EndAuctionNotificationViewModel data) {
        if (data == null) {
            return;
        }
        lblStatusIcon.setText(resolveIcon(data.getResultType()));
        lblTitle.setText(data.getMessageTitle());
        lblBody.setText(data.getMessageBody());
        lblAuctionId.setText(data.getAuctionId());
        lblItemName.setText(data.getItemName());
        lblFinalPrice.setText(formatPrice(data.getFinalPrice()));
        lblWinner.setText(data.getWinnerUsername());
        lblEndReason.setText(formatEnum(data.getEndReason().name()));
        lblTransaction.setText(data.isTransactionApplied() ? "Da ap dung" : "Khong ap dung");
        lblNextAction.setText(data.getNextActionHint());

        boolean seller = "SELLER".equalsIgnoreCase(data.getCurrentUserRole());
        btnSellerDashboard.setVisible(seller);
        btnSellerDashboard.setManaged(seller);
    }

    public void setActionHandlers(
            Runnable backToLobbyAction,
            Runnable sellerDashboardAction,
            Runnable viewOtherAuctionsAction
    ) {
        this.backToLobbyAction = backToLobbyAction;
        this.sellerDashboardAction = sellerDashboardAction;
        this.viewOtherAuctionsAction = viewOtherAuctionsAction;
    }

    @FXML
    private void backToLobby() {
        if (backToLobbyAction != null) {
            backToLobbyAction.run();
        }
    }

    @FXML
    private void openSellerDashboard() {
        if (sellerDashboardAction != null) {
            sellerDashboardAction.run();
        }
    }

    @FXML
    private void viewOtherAuctions() {
        if (viewOtherAuctionsAction != null) {
            viewOtherAuctionsAction.run();
        }
    }

    private void bindAction(Button button, Runnable action) {
        if (button != null) {
            button.setOnAction(event -> action.run());
        }
    }

    private String resolveIcon(EndAuctionResultType resultType) {
        return switch (resultType) {
            case BIDDER_WIN, SELLER_SOLD -> "WIN";
            case CLOSED_BY_ADMIN, CLOSED_BY_SELLER -> "STOP";
            case SELLER_NO_WINNER, NO_WINNER -> "END";
            default -> "INFO";
        };
    }

    private String formatPrice(double price) {
        return price > 0 ? String.format("%,.0f $", price) : "Chua co thong tin";
    }

    private String formatEnum(String value) {
        return value == null ? "Chua co thong tin" : value.replace('_', ' ');
    }
}
