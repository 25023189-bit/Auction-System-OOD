package com.auction.client.controllers;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
// SỬA: Import đúng từ package client
import com.auction.client.service.AuctionService;
import com.auction.client.network.ClientConnection;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Lớp AuctionController quản lý giao diện Client.
 * Đã chuẩn hóa việc truy cập dữ liệu qua Getter/Setter của lớp Message.
 */
public class AuctionController implements Initializable {

    @FXML private VBox paneLogin, paneSelectAuction, paneAuctionRoom, paneRegister, paneForgotPassword;
    @FXML private TextField txtUsername, txtRegUsername, txtForgotUsername;
    @FXML private PasswordField txtPassword, txtRegPassword, txtRegConfirm, txtForgotNewPassword, txtForgotConfirm;
    @FXML private Label lblStatus, lblRegStatus, lblForgotStatus;
    @FXML private ComboBox<String> cbRegRole;
    @FXML private FlowPane gridAuctions;
    @FXML private Button btnCreateAuction;
    @FXML private Label lblAuctionItemName, lblCurrentPrice, lblParticipantCount;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtBidAmount, txtChatInput;

    private ClientConnection clientConnection;
    private AuctionService auctionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo kết nối mạng
        clientConnection = new ClientConnection(this);
        // Khởi tạo service và truyền kết nối vào
        auctionService = new AuctionService(clientConnection);

        clientConnection.connect();

        if (cbRegRole != null) {
            cbRegRole.getItems().addAll("BIDDER", "SELLER");
            cbRegRole.setValue("BIDDER");
        }
    }

    @FXML private void showRegisterScreen() { switchScreen(paneRegister); if(lblRegStatus != null) lblRegStatus.setText(""); }
    @FXML private void showLoginScreen() { switchScreen(paneLogin); }
    @FXML public void showForgotPasswordScreen() { switchScreen(paneForgotPassword); if(lblForgotStatus != null) lblForgotStatus.setText(""); }

    private void switchScreen(VBox screenToShow) {
        if(paneLogin != null) paneLogin.setVisible(false);
        if(paneRegister != null) paneRegister.setVisible(false);
        if(paneSelectAuction != null) paneSelectAuction.setVisible(false);
        if(paneAuctionRoom != null) paneAuctionRoom.setVisible(false);
        if (paneForgotPassword != null) paneForgotPassword.setVisible(false);

        if(screenToShow != null) {
            screenToShow.setVisible(true);
            screenToShow.toFront();
        }
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đủ thông tin!");
            return;
        }
        auctionService.login(username, password);
    }

    @FXML
    private void handleSubmitRegister() {
        // Khớp với hàm register(role, username, password) trong AuctionService
        auctionService.register(cbRegRole.getValue(), txtRegUsername.getText().trim(), txtRegPassword.getText().trim());
    }

    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            auctionService.placeBid(amount);
            txtBidAmount.clear();
        } catch (NumberFormatException e) {
            txtChatLog.appendText("Hệ thống: Vui lòng nhập số tiền hợp lệ!\n");
        }
    }

    @FXML
    private void handleSendChat() {
        String msg = txtChatInput.getText().trim();
        if (!msg.isEmpty()) {
            auctionService.sendChat(msg);
            txtChatInput.clear();
        }
    }

    @FXML
    private void handleBackToSelection() {
        auctionService.leaveRoom();
        switchScreen(paneSelectAuction);
    }

    /**
     * Nhận và xử lý phản hồi từ Server.
     */
    public void onServerResponse(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                case "LOGIN_SUCCESS":
                    ClientConnection.currentUser = txtUsername.getText().trim();
                    auctionService.setCurrentUser(txtUsername.getText().trim());

                    lblStatus.setText("✅ Đăng nhập thành công!");
                    lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);

                    // Kiểm tra vai trò từ server gửi về (nằm trong ID)
                    String userRole = msg.getId();
                    if (userRole != null && userRole.equalsIgnoreCase("Seller")) {
                        btnCreateAuction.setVisible(true);
                        btnCreateAuction.setManaged(true);
                    } else {
                        btnCreateAuction.setVisible(false);
                        btnCreateAuction.setManaged(false);
                    }

                    // Lấy danh sách phòng ngay khi đăng nhập
                    auctionService.getRooms();
                    switchScreen(paneSelectAuction);
                    break;

                case "LOGIN_FAIL":
                    lblStatus.setText("❌ " + msg.getData());
                    lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                    break;

                case "REGISTER_SUCCESS":
                    lblStatus.setText("Đăng ký thành công! Hãy đăng nhập.");
                    lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                    showLoginScreen();
                    break;

                case "REGISTER_FAIL":
                    lblRegStatus.setText("❌ " + msg.getData());
                    break;

                case "ROOM_JOINED":
                    // Ép kiểu Object sang AuctionRoom model
                    if (msg.getData() instanceof AuctionRoom) {
                        AuctionRoom joinedRoom = (AuctionRoom) msg.getData();
                        lblAuctionItemName.setText(joinedRoom.getItemName());
                        lblCurrentPrice.setText("Giá hiện tại: " + joinedRoom.getCurrentPrice() + " $");
                    }
                    break;

                case "NEW_BID":
                    lblCurrentPrice.setText("Giá hiện tại: " + msg.getData() + " $");
                    txtChatLog.appendText("Hệ thống: [" + msg.getId() + "] đã trả: " + msg.getData() + "$\n");
                    break;

                case "CHAT_MSG":
                    txtChatLog.appendText("[" + msg.getId() + "]: " + msg.getData() + "\n");
                    break;

                case "BID_FAIL":
                    txtChatLog.appendText("Hệ thống: " + msg.getData() + "\n");
                    break;

                case "ROOM_LIST":
                    gridAuctions.getChildren().clear();
                    String roomsRaw = (String) msg.getData();
                    if (roomsRaw != null && !roomsRaw.isEmpty()) {
                        String[] rooms = roomsRaw.split(";");
                        for (String roomData : rooms) {
                            if (!roomData.isEmpty()) {
                                String[] info = roomData.split("\\|");
                                if (info.length >= 3) {
                                    createAuctionCard(info[0], info[1], info[2]);
                                }
                            }
                        }
                    }
                    break;

                case "CREATE_AUCTION_SUCCESS":
                    String autoJoinRoomId = msg.getId();
                    auctionService.joinRoom(autoJoinRoomId);
                    switchScreen(paneAuctionRoom);
                    break;
            }
        });
    }

    private void createAuctionCard(String roomId, String itemName, String sellerName) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; -fx-border-radius: 5;");
        card.setPrefSize(180, 140);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        Label lblName = new Label(itemName);
        lblName.setStyle("-fx-font-weight: bold;");
        Label lblId = new Label("ID: " + roomId);
        Label lblSeller = new Label("Seller: " + sellerName);
        lblSeller.setStyle("-fx-font-size: 10px;");

        Button btnJoin = new Button("Vào Phòng");
        btnJoin.setOnAction(e -> {
            auctionService.joinRoom(roomId);
            switchScreen(paneAuctionRoom);
        });

        card.getChildren().addAll(lblName, lblId, lblSeller, btnJoin);
        gridAuctions.getChildren().add(card);
    }

    public void updateConnectionStatus(String status) {
        Platform.runLater(() -> {
            if(lblStatus != null) lblStatus.setText("Trạng thái: " + status);
        });
    }

    @FXML
    private void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/views/seller-view.fxml"));
            javafx.scene.Parent root = loader.load();
            SellerController sellerCtrl = loader.getController();
            sellerCtrl.setAuctionService(this.auctionService);

            javafx.stage.Stage sellerStage = new javafx.stage.Stage();
            sellerStage.setTitle("Tạo phiên đấu giá - " + ClientConnection.currentUser);
            sellerStage.setScene(new javafx.scene.Scene(root));
            sellerStage.show();
        } catch (Exception e) {
            System.err.println("Lỗi mở màn hình Seller: " + e.getMessage());
        }
    }
}