package com.auction.client.feature.controllers.account.admin;

import com.auction.client.feature.controllers.account.admin.action.AdminActionSender;
import com.auction.client.feature.controllers.account.admin.dialog.AdminDialogController;
import com.auction.client.feature.controllers.account.admin.table.AdminTableBinder;
import com.auction.client.service.AuctionService;
import com.auction.client.shared.utils.ImageUtils;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.util.List;

public class AdminController {
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> colUserId, colUsername, colRole;
    @FXML private TableColumn<User, Double> colBalance;

    @FXML private TableView<AuctionRoom> tableAuctions;
    @FXML private TableColumn<AuctionRoom, String> colRoomId, colRoomName, colSeller, colStatus;
    @FXML private TableColumn<AuctionRoom, Double> colPrice, colBidStep;

    @FXML private TableView<BidTransaction> tableBidHistory;
    @FXML private TableColumn<BidTransaction, String> colBidAuctionId;
    @FXML private TableColumn<BidTransaction, String> colBidderId;
    @FXML private TableColumn<BidTransaction, Double> colBidAmount;
    @FXML private TableColumn<BidTransaction, String> colBidTime;

    @FXML private javafx.scene.image.ImageView imgPendingPreview;

    @FXML private TableView<PendingAuctionRequest> tablePendingAuctions;
    @FXML private TableColumn<PendingAuctionRequest, String> colPendingRequestId, colPendingSellerId,
            colPendingSellerOrganization, colPendingItemName, colPendingItemDesc;
    @FXML private TableColumn<PendingAuctionRequest, Double> colPendingStartingPrice, colPendingMinimumJoinAmount,
            colPendingBidStep, colPendingSellerReputation, colPendingSuccessfulAuctionRate, colPendingAdminCancellationRate;
    @FXML private TableColumn<PendingAuctionRequest, LocalDateTime> colPendingStartTime;
    @FXML private TableColumn<PendingAuctionRequest, Integer> colPendingDurationMinutes, colPendingExtensionSeconds;

    private AdminActionSender actionSender;
    private final AdminTableBinder tableBinder = new AdminTableBinder();
    private final AdminDialogController dialogController = new AdminDialogController();

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<AuctionRoom> auctionList = FXCollections.observableArrayList();
    private final ObservableList<BidTransaction> bidHistoryList = FXCollections.observableArrayList();
    private final ObservableList<PendingAuctionRequest> pendingAuctionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tableBinder.bindUsers(tableUsers, colUserId, colUsername, colRole, colBalance, userList);
        tableBinder.bindAuctions(tableAuctions, colRoomId, colRoomName, colSeller, colStatus, colPrice, colBidStep, auctionList);
        tableBinder.bindBidHistory(tableBidHistory, colBidAuctionId, colBidderId, colBidAmount, colBidTime, bidHistoryList);
        tableBinder.bindPendingAuctions(
                tablePendingAuctions,
                colPendingRequestId, colPendingSellerId, colPendingSellerOrganization, colPendingItemName, colPendingItemDesc,
                colPendingStartingPrice, colPendingMinimumJoinAmount, colPendingBidStep,
                colPendingSellerReputation, colPendingSuccessfulAuctionRate, colPendingAdminCancellationRate,
                colPendingStartTime, colPendingDurationMinutes, colPendingExtensionSeconds,
                pendingAuctionList
        );

        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && actionSender != null) {
                actionSender.loadBidHistory(newSelection.getRoomId());
            }
        });
        tablePendingAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Khi Admin bấm vào 1 dòng, ta lấy cái base64Image của dòng đó ra giải mã
            if (newVal != null && newVal.getBase64Image() != null) {
                // Gọi bùa giải mã từ class ImageUtils vừa tạo
                ImageUtils.applyBase64OrPlaceholder(imgPendingPreview, newVal.getBase64Image());
            } else {
                // Nếu phòng đó không có ảnh thì xóa trắng ImageView
                ImageUtils.applyBase64OrPlaceholder(imgPendingPreview, null);
            }
        });
    }

    public void setAuctionService(AuctionService auctionService) {
        this.actionSender = new AdminActionSender(auctionService);
        loadUsers();
        loadAuctions();
        loadPendingAuctions();
    }

    @FXML
    private void loadUsers() {
        actionSender.loadUsers();
    }

    @FXML
    private void loadAuctions() {
        actionSender.loadAuctions();
    }

    @FXML
    private void loadPendingAuctions() {
        actionSender.loadPendingAuctions();
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            dialogController.showWarning("No User Selected", "Please select a user to delete.");
            return;
        }
        if (dialogController.confirm("Confirm Delete", "Are you sure you want to delete account: " + selectedUser.getUsername() + "?")) {
            actionSender.deleteUser(selectedUser.getId());
        }
    }

    @FXML
    private void handleForceDeleteAuction() {
        AuctionRoom selectedRoom = tableAuctions.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            dialogController.showWarning("No Auction Selected", "Please select an auction to cancel.");
            return;
        }
        if (dialogController.confirm("Confirm Auction Cancellation", "Are you sure you want to force-cancel auction: " + selectedRoom.getRoomId() + "?")) {
            actionSender.deleteAuction(selectedRoom.getRoomId());
        }
    }

    @FXML
    private void handleApprovePendingAuction() {
        PendingAuctionRequest selectedRequest = tablePendingAuctions.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            dialogController.showWarning("No Request Selected", "Please select a pending auction request to approve.");
            return;
        }
        actionSender.approvePendingAuction(selectedRequest.getRequestId());
    }

    @FXML
    private void handleRejectPendingAuction() {
        PendingAuctionRequest selectedRequest = tablePendingAuctions.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            dialogController.showWarning("No Request Selected", "Please select a pending auction request to reject.");
            return;
        }
        actionSender.rejectPendingAuction(selectedRequest.getRequestId());
    }

    public void updateUsersTable(List<User> users) {
        Platform.runLater(() -> {
            userList.clear();
            if (users != null) userList.addAll(users);
        });
    }

    public void updateAuctionsTable(List<AuctionRoom> rooms) {
        Platform.runLater(() -> {
            auctionList.clear();
            if (rooms != null) auctionList.addAll(rooms);
        });
    }

    public void updateBidHistoryTable(List<BidTransaction> historyData) {
        Platform.runLater(() -> {
            bidHistoryList.clear();
            if (historyData != null) bidHistoryList.addAll(historyData);
        });
    }

    public void updatePendingAuctionsTable(List<PendingAuctionRequest> requests) {
        Platform.runLater(() -> {
            pendingAuctionList.clear();
            if (requests != null) pendingAuctionList.addAll(requests);
        });
    }

    public void handleAdminResponse(String action, String message) {
        Platform.runLater(() -> {
            if (action.contains("SUCCESS")) {
                dialogController.showInfo("Success", message);
                if (action.contains("USER")) loadUsers();
                if (action.contains("AUCTION")) {
                    loadAuctions();
                    loadPendingAuctions();
                }
            } else {
                dialogController.showError("Failed", message);
            }
        });
    }
}
