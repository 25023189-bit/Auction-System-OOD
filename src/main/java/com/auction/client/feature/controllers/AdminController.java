package com.auction.client.feature.controllers;

import com.auction.common.dto.Message;
import com.auction.common.model.*;
import com.auction.server.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.*;

public class AdminController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> colUserId, colUsername, colRole;
    @FXML private TableColumn<User, Double> colBalance;

    @FXML private TableView<AuctionRoom> tableAuctions;
    @FXML private TableColumn<AuctionRoom, String> colRoomId, colRoomName, colSeller, colStatus;
    @FXML private TableColumn<AuctionRoom, Double> colPrice;

    @FXML private TableView<BidTransaction> tableBidHistory;
    @FXML private TableColumn<BidTransaction, String> colBidAuctionId;
    @FXML private TableColumn<BidTransaction, String> colBidderId;
    @FXML private TableColumn<BidTransaction, Double> colBidAmount;
    @FXML private TableColumn<BidTransaction, String> colBidTime;

    private AuctionService auctionService;

    // Danh sách quan sát để tự động cập nhật UI khi có dữ liệu mới
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<AuctionRoom> auctionList = FXCollections.observableArrayList();
    private ObservableList<BidTransaction> bidHistoryList = FXCollections.observableArrayList();

    // ==========================================================
    // KHỞI TẠO GIAO DIỆN VÀ RÀNG BUỘC CỘT
    // ==========================================================
    @FXML
    public void initialize() {
        // Bảng User:
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        tableUsers.setItems(userList);

        // Bảng Auction:
        colRoomId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("nameSeller"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableAuctions.setItems(auctionList);

        colBidAuctionId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colBidderId.setCellValueFactory(new PropertyValueFactory<>("bidderId"));
        colBidAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colBidTime.setCellValueFactory(new PropertyValueFactory<>("bidTime"));

        tableBidHistory.setItems(bidHistoryList);

        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String selectedRoomId = newSelection.getRoomId();
                auctionService.getBidHistory(selectedRoomId);
            }
        });
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
        loadUsers();
        loadAuctions();
    }

    @FXML
    private void loadUsers() {
        // Gửi lệnh ADMIN_GET_USERS lên Server
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_USERS", auctionService.getCurrentUser(), "")
        );
    }

    @FXML
    private void loadAuctions() {
        // Lấy danh sách tất cả các phòng (kể cả đang chạy hay đã đóng)
        auctionService.getClientConnection().sendMessage(
                new Message("ADMIN_GET_AUCTIONS", auctionService.getCurrentUser(), "")
        );
    }

    // ==========================================================
    // CÁC HÀM XỬ LÝ NÚT BẤM (XÓA / HỦY)
    // ==========================================================
    @FXML
    private void handleDeleteUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn người dùng", "Vui lòng chọn một người dùng trong bảng để xóa!");
            return;
        }

        // Cảnh báo xác nhận trước khi xóa
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa tài khoản: " + selectedUser.getUsername() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Gửi lệnh xóa lên Server
            auctionService.getClientConnection().sendMessage(
                    new Message("ADMIN_DELETE_USER", auctionService.getCurrentUser(), selectedUser.getId())
            );
        }
    }

    @FXML
    private void handleForceDeleteAuction() {
        AuctionRoom selectedRoom = tableAuctions.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn phiên", "Vui lòng chọn một phiên đấu giá để hủy!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy phiên");
        confirm.setHeaderText("Bạn có chắc muốn ép hủy phiên: " + selectedRoom.getRoomId() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Gửi lệnh hủy phiên lên Server
            auctionService.getClientConnection().sendMessage(
                    new Message("ADMIN_DELETE_AUCTION", auctionService.getCurrentUser(), selectedRoom.getRoomId())
            );
        }
    }

    // ==========================================================
    // CẬP NHẬT GIAO DIỆN TỪ SERVER TRẢ VỀ (Được gọi từ Router)
    // ==========================================================
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

    public void handleAdminResponse(String action, String message) {
        Platform.runLater(() -> {
            if (action.contains("SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", message);
                // Refresh lại bảng sau khi xóa thành công
                if (action.contains("USER")) loadUsers();
                if (action.contains("AUCTION")) loadAuctions();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", message);
            }
        });
    }

    // Hàm tiện ích hiển thị thông báo
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}