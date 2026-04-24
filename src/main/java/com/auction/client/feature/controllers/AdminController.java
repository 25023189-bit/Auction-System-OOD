package com.auction.client.feature.controllers;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import com.auction.server.service.AuctionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @FXML private TableView<PendingAuctionRequest> tablePendingAuctions;
    @FXML private TableColumn<PendingAuctionRequest, String> colPendingRequestId, colPendingSellerId,
            colPendingSellerOrganization, colPendingItemName, colPendingItemDesc;
    @FXML private TableColumn<PendingAuctionRequest, Double> colPendingStartingPrice, colPendingMinimumJoinAmount,
            colPendingBidStep, colPendingSellerReputation, colPendingSuccessfulAuctionRate, colPendingAdminCancellationRate;
    @FXML private TableColumn<PendingAuctionRequest, LocalDateTime> colPendingStartTime;
    @FXML private TableColumn<PendingAuctionRequest, Integer> colPendingDurationMinutes, colPendingExtensionSeconds;

    private AuctionService auctionService;

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<AuctionRoom> auctionList = FXCollections.observableArrayList();
    private final ObservableList<BidTransaction> bidHistoryList = FXCollections.observableArrayList();
    private final ObservableList<PendingAuctionRequest> pendingAuctionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        tableUsers.setItems(userList);

        colRoomId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("nameSeller"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colBidStep.setCellValueFactory(new PropertyValueFactory<>("bidStep"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableAuctions.setItems(auctionList);

        colBidAuctionId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colBidderId.setCellValueFactory(new PropertyValueFactory<>("bidderId"));
        colBidAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colBidTime.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        tableBidHistory.setItems(bidHistoryList);

        colPendingRequestId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colPendingSellerId.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        colPendingSellerOrganization.setCellValueFactory(new PropertyValueFactory<>("sellerOrganization"));
        colPendingItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colPendingItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        colPendingStartingPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colPendingMinimumJoinAmount.setCellValueFactory(new PropertyValueFactory<>("minimumJoinAmount"));
        colPendingBidStep.setCellValueFactory(new PropertyValueFactory<>("bidStep"));
        colPendingStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colPendingDurationMinutes.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        colPendingExtensionSeconds.setCellValueFactory(new PropertyValueFactory<>("extensionSeconds"));
        colPendingSellerReputation.setCellValueFactory(new PropertyValueFactory<>("sellerReputation"));
        colPendingSuccessfulAuctionRate.setCellValueFactory(new PropertyValueFactory<>("successfulAuctionRate"));
        colPendingAdminCancellationRate.setCellValueFactory(new PropertyValueFactory<>("adminCancellationRate"));
        tablePendingAuctions.setItems(pendingAuctionList);

        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                auctionService.getBidHistory(newSelection.getRoomId());
            }
        });
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
        loadUsers();
        loadAuctions();
        loadPendingAuctions();
    }

    @FXML
    private void loadUsers() {
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_USERS", auctionService.getCurrentUser(), "")
        );
    }

    @FXML
    private void loadAuctions() {
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_AUCTIONS", auctionService.getCurrentUser(), "")
        );
    }

    @FXML
    private void loadPendingAuctions() {
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_PENDING_AUCTIONS", auctionService.getCurrentUser(), "")
        );
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No User Selected", "Please select a user to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete account: " + selectedUser.getUsername() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            auctionService.getClientConnection().sendMessage(
                    new Message("ADMIN_DELETE_USER", auctionService.getCurrentUser(), selectedUser.getId())
            );
        }
    }

    @FXML
    private void handleForceDeleteAuction() {
        AuctionRoom selectedRoom = tableAuctions.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Auction Selected", "Please select an auction to cancel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Auction Cancellation");
        confirm.setHeaderText("Are you sure you want to force-cancel auction: " + selectedRoom.getRoomId() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            auctionService.getClientConnection().sendMessage(
                    new Message("ADMIN_DELETE_AUCTION", auctionService.getCurrentUser(), selectedRoom.getRoomId())
            );
        }
    }

    @FXML
    private void handleApprovePendingAuction() {
        PendingAuctionRequest selectedRequest = tablePendingAuctions.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert(Alert.AlertType.WARNING, "No Request Selected", "Please select a pending auction request to approve.");
            return;
        }

        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_APPROVE_AUCTION", auctionService.getCurrentUser(), selectedRequest.getRequestId())
        );
    }

    @FXML
    private void handleRejectPendingAuction() {
        PendingAuctionRequest selectedRequest = tablePendingAuctions.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert(Alert.AlertType.WARNING, "No Request Selected", "Please select a pending auction request to reject.");
            return;
        }

        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_REJECT_AUCTION", auctionService.getCurrentUser(), selectedRequest.getRequestId())
        );
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
            if (historyData != null) {
                bidHistoryList.addAll(historyData);
            }
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
                showAlert(Alert.AlertType.INFORMATION, "Success", message);
                if (action.contains("USER")) loadUsers();
                if (action.contains("AUCTION")) loadAuctions();
                if (action.contains("AUCTION")) loadPendingAuctions();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", message);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
