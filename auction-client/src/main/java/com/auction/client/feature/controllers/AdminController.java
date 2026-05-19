package com.auction.client.feature.controllers;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import com.auction.client.service.AuctionService;
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

/**
 * Controller cho cửa sổ quản trị của admin.
 *
 * Vai trò:
 * - Hiển thị và cập nhật bảng user, auction, bid history và pending auction request.
 * - Gửi các lệnh admin như xóa user, hủy auction, approve/reject request lên server.
 *
 * Luồng chính:
 * 1. initialize() cấu hình TableView/column binding và listener chọn auction để tải bid history.
 * 2. setAuctionService() nhận service hiện tại, load dữ liệu admin và các handler cập nhật bảng khi server phản hồi.
 *
 * Business rules:
 * - Xóa user và hủy auction phải có item được chọn và cần xác nhận trước khi gửi lệnh.
 * - Approve/reject pending auction chỉ gửi requestId, server chịu trách nhiệm tạo hoặc loại bỏ request.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: TableView/ObservableList JavaFX được cập nhật qua Platform.runLater khi nhận response.
 * - Dependency: AuctionService, Message, JavaFX TableView/TableColumn/Alert, User, AuctionRoom, BidTransaction, PendingAuctionRequest.
 */
public class AdminController {

    @FXML
    private TableView<User> tableUsers;
    @FXML
    private TableColumn<User, String> colUserId, colUsername, colRole;
    @FXML
    private TableColumn<User, Double> colBalance;

    @FXML
    private TableView<AuctionRoom> tableAuctions;
    @FXML
    private TableColumn<AuctionRoom, String> colRoomId, colRoomName, colSeller, colStatus;
    @FXML
    private TableColumn<AuctionRoom, Double> colPrice, colBidStep;

    @FXML
    private TableView<BidTransaction> tableBidHistory;
    @FXML
    private TableColumn<BidTransaction, String> colBidAuctionId;
    @FXML
    private TableColumn<BidTransaction, String> colBidderId;
    @FXML
    private TableColumn<BidTransaction, Double> colBidAmount;
    @FXML
    private TableColumn<BidTransaction, String> colBidTime;

    @FXML
    private TableView<PendingAuctionRequest> tablePendingAuctions;
    @FXML
    private TableColumn<PendingAuctionRequest, String> colPendingRequestId, colPendingSellerId,
            colPendingSellerOrganization, colPendingItemName, colPendingItemDesc;
    @FXML
    private TableColumn<PendingAuctionRequest, Double> colPendingStartingPrice, colPendingMinimumJoinAmount,
            colPendingBidStep, colPendingSellerReputation, colPendingSuccessfulAuctionRate, colPendingAdminCancellationRate;
    @FXML
    private TableColumn<PendingAuctionRequest, LocalDateTime> colPendingStartTime;
    @FXML
    private TableColumn<PendingAuctionRequest, Integer> colPendingDurationMinutes, colPendingExtensionSeconds;

    // Service được truyền từ launcher sau khi FXML tạo controller.
    private AuctionService auctionService;

    // ObservableList là nguồn dữ liệu trực tiếp cho TableView JavaFX.
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<AuctionRoom> auctionList = FXCollections.observableArrayList();
    private final ObservableList<BidTransaction> bidHistoryList = FXCollections.observableArrayList();
    private final ObservableList<PendingAuctionRequest> pendingAuctionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // PropertyValueFactory map tên thuộc tính model sang cột hiển thị trên bảng.
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

        // Khi chọn một phòng, client yêu cầu server trả về lịch sử bid của phòng đó.
        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                auctionService.getBidHistory(newSelection.getRoomId());
            }
        });
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
        // Sau khi có service mới có thể gửi request lấy dữ liệu admin.
        loadUsers();
        loadAuctions();
        loadPendingAuctions();
    }

    @FXML
    private void loadUsers() {
        // Gửi action riêng để server phân biệt request lấy danh sách user.
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_USERS", auctionService.getCurrentUser(), "")
        );
    }

    @FXML
    private void loadAuctions() {
        // Lấy toàn bộ phiên đấu giá để admin theo dõi hoặc hủy phiên.
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_AUCTIONS", auctionService.getCurrentUser(), "")
        );
    }

    @FXML
    private void loadPendingAuctions() {
        // Lấy các yêu cầu tạo phiên đang chờ admin phê duyệt.
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

        // Thao tác xóa user là hành động nhạy cảm nên cần xác nhận trước khi gửi lệnh.
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

        // Admin có quyền hủy cưỡng bức phiên đấu giá khi cần xử lý vi phạm.
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

        // Chỉ gửi requestId, phần kiểm tra và tạo phòng thật do server xử lý.
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

        // Từ chối yêu cầu tạo phiên và để server cập nhật trạng thái lưu trữ.
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_REJECT_AUCTION", auctionService.getCurrentUser(), selectedRequest.getRequestId())
        );
    }

    public void updateUsersTable(List<User> users) {
        // Server response có thể về từ thread mạng, vì vậy cập nhật TableView qua Platform.runLater.
        Platform.runLater(() -> {
            userList.clear();
            if (users != null) userList.addAll(users);
        });
    }

    public void updateAuctionsTable(List<AuctionRoom> rooms) {
        // Thay toàn bộ danh sách để bảng luôn phản ánh trạng thái mới nhất từ server.
        Platform.runLater(() -> {
            auctionList.clear();
            if (rooms != null) auctionList.addAll(rooms);
        });
    }

    public void updateBidHistoryTable(List<BidTransaction> historyData) {
        // Lịch sử bid phụ thuộc phòng đang chọn ở bảng auction.
        Platform.runLater(() -> {
            bidHistoryList.clear();
            if (historyData != null) {
                bidHistoryList.addAll(historyData);
            }
        });
    }

    public void updatePendingAuctionsTable(List<PendingAuctionRequest> requests) {
        // Danh sách yêu cầu chờ duyệt được reload sau khi admin approve/reject.
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
