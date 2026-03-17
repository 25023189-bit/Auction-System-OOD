package client.controllers;

import client.network.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import shared.Message;

public class AuctionController {
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblWinner;

    @FXML private HBox bidderBox;
    @FXML private HBox sellerBox;
    @FXML private TextField txtBidAmount;

    private ServerConnection conn = ServerConnection.getInstance();
    private String myUsername;
    private String currentPhien;

    // Hàm này được LoginController gọi sang để truyền thông tin người dùng
    public void setupUser(String username, String role, String maPhien) {
    }

    private void processMessage(Message msg) {
    }

    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            conn.send(new Message("BID", myUsername, amount));
            txtBidAmount.clear();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập số hợp lệ!").show();
        }
    }

    // --- CÁC HÀM CỦA SELLER ---
    @FXML private void handleAdd() { System.out.println("Gửi lệnh ADD_ITEM..."); }
    @FXML private void handleUpdate() { System.out.println("Gửi lệnh UPDATE_PRICE..."); }
    @FXML private void handleDelete() { System.out.println("Gửi lệnh DELETE_ITEM..."); }
}