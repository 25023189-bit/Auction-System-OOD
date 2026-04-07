package com.auction.client.controllers;

import com.auction.common.model.BidTransaction;
import com.auction.server.service.AuctionService;
import com.auction.server.service.ClientConnection;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Seller;
import com.auction.common.model.User;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.auction.common.dto.Message;
import javafx.fxml.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AuctionController implements Initializable {
    // ==========================================================
    // KHAI BÁO CÁC THÀNH PHẦN GIAO DIỆN (@FXML)
    // ==========================================================
    @FXML private VBox paneLogin, paneAuctionRoom, paneRegister, paneForgotPassword;

    @FXML private TextField txtUsername, txtRegUsername, txtForgotUsername;
    @FXML private PasswordField txtPassword, txtRegPassword, txtRegConfirm, txtForgotNewPassword, txtForgotConfirm;
    @FXML private Label lblStatus, lblRegStatus, lblForgotStatus;
    @FXML private ComboBox<String> cbRegRole;
    @FXML private Label lblUsername, lblBalance, lblUsernameDisplay;

    @FXML private Button btnCloseAuction, btnCreateAuction;

    @FXML private FlowPane paneSelectAuction; // Container chứa các phòng (Cột giữa)

    @FXML private Label lblAuctionItemName, lblCurrentPrice, lblParticipantCount;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtBidAmount, txtChatInput;

    @FXML private BorderPane paneMainLobby;
    @FXML private VBox vboxCurrencyRates, vboxNews;

    // ==========================================================
    // KHAI BÁO CÁC TẦNG DỊCH VỤ (SERVICES)
    // ==========================================================
    private ClientConnection clientConnection;
    private AuctionService auctionService;
    private String currentRoomId;
    private String myUsername = "";
    private User currentUserProfile;
    private AdminController adminController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientConnection = new ClientConnection(this);
        auctionService = new AuctionService(clientConnection);
        clientConnection.connect();

        if (cbRegRole != null) {
            cbRegRole.getItems().addAll("BIDDER", "SELLER");
            cbRegRole.setValue("BIDDER");
        }
    }

    // ==========================================================
    // CÁC HÀM ĐIỀU KHIỂN CHUYỂN MÀN HÌNH (GỌN GÀNG, KHÔNG LỖI)
    // ==========================================================
    private void switchScreen(javafx.scene.layout.Pane screenToShow) {
        if (paneLogin != null) paneLogin.setVisible(false);
        if (paneRegister != null) paneRegister.setVisible(false);
        if (paneAuctionRoom != null) paneAuctionRoom.setVisible(false);
        if (paneForgotPassword != null) paneForgotPassword.setVisible(false);
        if (paneMainLobby != null) paneMainLobby.setVisible(false);

        if (screenToShow != null) {
            screenToShow.setVisible(true);
            screenToShow.toFront();
        }
    }

    private void showMainLobby() {
        switchScreen(paneMainLobby);
    }

    private void showAuctionRoom() {
        switchScreen(paneAuctionRoom);
    }

    @FXML private void showRegisterScreen() { switchScreen(paneRegister); lblRegStatus.setText(""); }
    @FXML private void showLoginScreen() { switchScreen(paneLogin); }
    @FXML public void showForgotPasswordScreen(){ switchScreen(paneForgotPassword); lblForgotStatus.setText(""); }

    // ==========================================================
    // XỬ LÝ SỰ KIỆN TỪ GIAO DIỆN (USER ACTIONS)
    // ==========================================================
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
    private void handleLogout() {
        openLoginScreen();
    }

    //Sau này triển khai thành phương thức trừu tượng handleSubmitRegister và handleSubmitForgotPassword
    @FXML
    private void handleSubmitRegister() {
        String user = txtRegUsername.getText().trim();
        String password = txtRegPassword.getText().trim();
        String roleValue = cbRegRole.getValue();
        //String roleToSend = (roleValue != null && roleValue.contains("SELLER")) ? "SELLER" : "BIDDER";
        if (password.isEmpty() || user.isEmpty() || !password.equals(txtRegConfirm.getText().trim())) {
            lblRegStatus.setText("Thông tin không hợp lệ hoặc mật khẩu không khớp!");
        }else{
            if (roleValue.equals("SELLER") || roleValue.equals("ADMIN")){
                auctionService.register(user, password, roleValue);
            }else{
                auctionService.register(user, password, "BIDDER");
            }
        }
    }

    @FXML
    private void handleSubmitForgotPassword() {
        String userForgotPassword = txtForgotUsername.getText().trim();
        String pass = txtForgotNewPassword.getText().trim();
        if (userForgotPassword.isEmpty() || pass.isEmpty() || !pass.equals(txtForgotConfirm.getText().trim())) {
            lblForgotStatus.setText("Thông tin không hợp lệ hoặc mật khẩu không khớp!");
            return;
        }
        auctionService.resetPassword(userForgotPassword, pass);
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
        showMainLobby(); // Quay lại sảnh an toàn
    }

    @FXML
    private void handleCloseAuction() {
        if (this.currentRoomId != null) {
            auctionService.closeAuction(this.currentRoomId);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Phòng đấu giá đã được đóng. Chúc mừng bạn đã bán thành công!");
            alert.setHeaderText("Chốt đơn thành công");
            alert.showAndWait();

            showMainLobby(); // Chốt đơn xong đá ra sảnh chính
            this.currentRoomId = null;
        }
    }

    // ==========================================================
    // NHẬN VÀ XỬ LÝ PHẢN HỒI TỪ SERVER (ROUTER)
    // ==========================================================
    public void onServerResponse(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getAction()) {
                case "LOGIN_SUCCESS":
                    User loggedInUser = (User) msg.data;
                    System.out.println("Role nhận được từ Server: " + loggedInUser.getRole()); // In ra để kiểm tra
                    this.currentUserProfile = loggedInUser;
                    String userId = loggedInUser.getId();
                    ClientConnection.currentUser = userId;
                    auctionService.setCurrentUser(userId);
                    this.myUsername = loggedInUser.getUsername();

                    if (lblUsername != null) lblUsername.setText("Xin chào: " + loggedInUser.getUsername());
                    if (lblBalance != null) lblBalance.setText("Số dư ví: " + String.format("%,.0f $", loggedInUser.getBalance()));

                    lblStatus.setText("✅ Đăng nhập thành công!");
                    lblStatus.setTextFill(Color.GREEN);

                    if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                        // NẾU LÀ ADMIN -> MỞ CỬA SỔ QUẢN TRỊ
                        openAdminDashboard();

                        // Ẩn cửa sổ đăng nhập hiện tại đi (Tùy chọn)
                        paneLogin.getScene().getWindow().hide();
                    } else {
                        // NẾU LÀ USER BÌNH THƯỜNG -> VÀO SẢNH NHƯ CŨ
                        if (lblUsername != null) lblUsername.setText("Xin chào: " + loggedInUser.getUsername());
                        if (lblBalance != null) lblBalance.setText("Số dư ví: " + String.format("%,.0f $", loggedInUser.getBalance()));

                        boolean isSeller = (loggedInUser instanceof Seller);
                        if (btnCreateAuction != null) {
                            btnCreateAuction.setVisible(isSeller);
                            btnCreateAuction.setManaged(isSeller);
                        }

                        Stage stage = (Stage) paneLogin.getScene().getWindow();
                        stage.setMaximized(true);

                        clientConnection.sendMessage(new Message("GET_ROOMS", userId, ""));
                        showMainLobby();
                    }
                    break;

                case "LOGIN_FAIL":
                    lblStatus.setText("❌ " + msg.data);
                    lblStatus.setTextFill(Color.RED);
                    break;

                case "REGISTER_SUCCESS":
                    lblStatus.setTextFill(Color.GREEN);
                    showLoginScreen();
                    txtUsername.setText(txtRegUsername.getText());
                    txtPassword.clear();
                    break;

                case "REGISTER_FAIL":
                    lblRegStatus.setText("❌ " + msg.data);
                    break;

                // Xử lý khi Server báo đổi mật khẩu THÀNH CÔNG
                case "RESET_SUCCESS":
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
                    alert.setHeaderText("Thành công");
                    alert.showAndWait();

                    // Chuyển về màn hình đăng nhập và dọn dẹp các ô nhập liệu cũ
                    showLoginScreen();
                    txtForgotUsername.clear();
                    txtForgotNewPassword.clear();
                    txtForgotConfirm.clear();
                    break;

                // Xử lý khi Server báo đổi mật khẩu THẤT BẠI (VD: Không tìm thấy tài khoản)
                case "RESET_FAIL":
                    if (lblForgotStatus != null) {
                        lblForgotStatus.setText("❌ " + msg.data);
                        lblForgotStatus.setTextFill(Color.RED);
                    }
                    break;

                case "ROOM_JOINED":
                    AuctionRoom room = (AuctionRoom) msg.data;
                    this.currentRoomId = room.getRoomId();

                    lblAuctionItemName.setText(room.getItemName());
                    lblCurrentPrice.setText("Giá hiện tại: " + String.format("%,.0f $", room.getCurrentPrice()));

                    boolean isOwner = room.getNameSeller() != null && room.getNameSeller().trim().equalsIgnoreCase(myUsername.trim());
                    if (btnCloseAuction != null) {
                        btnCloseAuction.setVisible(isOwner);
                        btnCloseAuction.setManaged(isOwner);
                    }

                    // CHỈ CHUYỂN SANG MÀN HÌNH ĐẤU GIÁ KHI ĐÃ CÓ DATA TỪ SERVER
                    showAuctionRoom();
                    break;

                case "CHAT_MSG":
                    // Trả lại code hiển thị đơn giản cho Client
                    txtChatLog.appendText("[" + msg.username + "]: " + msg.data + "\n");
                    break;

                case "ROOM_LIST":
                    System.out.println("\n====== 🐞 BẮT ĐẦU DEBUG CHỨC NĂNG TẢI PHÒNG ======");
                    System.out.println("1. Client nhận được lệnh ROOM_LIST từ Server.");
                    System.out.println("2. Dữ liệu thô (msg.data) gửi về là: [" + msg.data + "]");

                    // ÉP BUỘC PHẢI BỌC TRONG PLATFORM.RUNLATER KHI VẼ GIAO DIỆN
                    Platform.runLater(() -> {
                        // BƯỚC A: Kiểm tra xem code có tìm thấy cái khung chứa phòng trên FXML không
                        if (paneSelectAuction == null) {
                            System.out.println("❌ LỖI NGHIÊM TRỌNG: paneSelectAuction bị NULL! File FXML của bạn đang bị thiếu fx:id=\"paneSelectAuction\" ở thẻ FlowPane.");
                            return; // Dừng luôn, không vẽ nữa
                        }

                        System.out.println("3. FXML ổn! Đang dọn dẹp các phòng cũ trên màn hình...");
                        paneSelectAuction.getChildren().clear();

                        // BƯỚC B: Kiểm tra xem dữ liệu có bị rỗng không
                        if (msg.data == null || msg.data.toString().trim().isEmpty()) {
                            System.out.println("⚠️ CẢNH BÁO: Dữ liệu Server gửi về bị trống. Chắc chắn là chưa có phòng nào được tạo trên Server!");
                            System.out.println("===================================================\n");
                            return;
                        }

                        // BƯỚC C: Tách dữ liệu và tiến hành vẽ
                        String rawData = (String) msg.data;
                        String[] rooms = rawData.split(";");
                        System.out.println("4. Tìm thấy " + rooms.length + " phòng trong chuỗi dữ liệu. Bắt đầu vẽ...");

                        int successCount = 0;
                        for (String roomData : rooms) {
                            if (!roomData.isEmpty()) {
                                String[] info = roomData.split("\\|");
                                if (info.length >= 3) {
                                    System.out.println(" -> Đang tạo thẻ (Card) cho phòng: ID=" + info[0] + ", Tên=" + info[1]);
                                    createAuctionCard(info[0], info[1], info[2]);
                                    successCount++;
                                } else {
                                    System.out.println("❌ Lỗi format: Chuỗi phòng này bị thiếu dữ liệu (Cần có mã, tên, chủ phòng): " + roomData);
                                }
                            }
                        }

                        System.out.println("5. Hoàn tất! Đã vẽ thành công " + successCount + " phòng.");
                        System.out.println("6. Trạng thái hiển thị thực tế trên màn hình: Visible = " + paneSelectAuction.isVisible());
                        System.out.println("===================================================\n");
                    });
                    break;

                case "CREATE_AUCTION_SUCCESS":
                    currentRoomId = msg.id;
                    auctionService.joinRoom(currentRoomId);
                    // Không cần gọi showAuctionRoom ở đây vì ROOM_JOINED sẽ tự gọi
                    break;

                case "UPDATE_ROOMS":
                    auctionService.getRooms();
                    break;

                case "BID_FAIL":
                    txtChatLog.appendText("❌ " + msg.data + "\n");
                    break;

                case "BID_SUCCESS":
                    lblCurrentPrice.setText("Giá hiện tại: " + String.format("%,.0f $", (Double) msg.data));
                    break;

                case "AUCTION_CLOSED_NOTIFY":
                    if (this.currentRoomId != null && this.currentRoomId.equals((String) msg.data)) {
                        Alert alerts = new Alert(Alert.AlertType.WARNING, "Chủ phòng đã chốt đơn thành công! Bạn sẽ được đưa về sảnh chính.");
                        alerts.setHeaderText("Phiên đấu giá kết thúc");
                        alerts.showAndWait();

                        showMainLobby(); // Đá người chơi về sảnh chính
                        this.currentRoomId = null;
                    }
                    break;

                case "UPDATE_PRICE":
                    String[] parts = ((String) msg.data).split("\\|");
                    if (this.currentRoomId != null && this.currentRoomId.equals(parts[0])) {
                        lblCurrentPrice.setText(parts[1] + " $");
                        txtChatLog.appendText("📢 Giá mới: " + parts[2] + " đang giữ giá " + parts[1] + "$\n");
                    }
                    break;

                case "UPDATE_BALANCE":
                    if (msg.id.equals(auctionService.getCurrentUser())) {
                        double newBalance = (Double) msg.data;
                        if (this.currentUserProfile != null) this.currentUserProfile.setBalance(newBalance);
                        if (lblBalance != null) lblBalance.setText("Số dư: " + String.format("%,.0f $", newBalance));

                        //Alert dùng để tạo các hộp thoại thông báo (dialog box) dựng sẵn
                        Alert alerts = new Alert(Alert.AlertType.INFORMATION, "Số dư hiện tại của bạn là: " + String.format("%,.0f $", newBalance));
                        alerts.setHeaderText("Biến động số dư");
                        alerts.show();
                    }
                    break;

                case "ADMIN_USER_LIST":
                    System.out.println("CLIENT ĐÃ NHẬN ĐƯỢC LIST TỪ SERVER!");
                    if (adminController != null) {
                        adminController.updateUsersTable((java.util.List<User>) msg.data);
                    }else{
                        System.out.println("LỖI: adminController BỊ NULL !!!");
                    }
                    break;

                case "ADMIN_AUCTION_LIST":
                    if (adminController != null) {
                        adminController.updateAuctionsTable((java.util.List<AuctionRoom>) msg.data);
                    }
                    break;

                // Thêm vào khối switch-case xử lý dữ liệu trả về từ Server ở Client
                case "BID_HISTORY_SUCCESS":
                    @SuppressWarnings("unchecked")
                    List<BidTransaction> receivedHistory = (List<BidTransaction>) msg.data;

                    Platform.runLater(() -> {
                        if (this.adminController != null) {
                            this.adminController.updateBidHistoryTable(receivedHistory);
                        }
                    });
                    break;

                case "ADMIN_ACTION_SUCCESS":
                    if (adminController != null) {
                        adminController.handleAdminResponse(msg.getAction() + "_" + msg.id, (String) msg.data);
                    }
                    break;

                case "ADMIN_ACTION_FAIL":
                    if (adminController != null) {
                        adminController.handleAdminResponse("FAIL", (String) msg.data);
                    }
                    break;

                case "BANNED":
                    Platform.runLater(() -> {
                        Alert alerted = new Alert(Alert.AlertType.ERROR);
                        alerted.setTitle("Thông báo hệ thống");
                        alerted.setHeaderText("TÀI KHOẢN ĐÃ BỊ VÔ HIỆU HÓA");
                        alerted.setContentText((String) msg.data);
                        alerted.showAndWait();

                        openLoginScreen();

                        // THUẬT TOÁN ĐÓNG CỬA SỔ AN TOÀN (Không bao giờ bị lỗi NullPointer)
                        // Lấy danh sách toàn bộ các cửa sổ đang mở trong App
                        java.util.List<javafx.stage.Window> openWindows = new java.util.ArrayList<>(javafx.stage.Window.getWindows());

                        for (javafx.stage.Window window : openWindows) {
                            if (window instanceof javafx.stage.Stage) {
                                javafx.stage.Stage stage = (javafx.stage.Stage) window;

                                // Nếu tên cửa sổ KHÔNG PHẢI là cửa sổ Đăng nhập thì đóng nó lại (Giải tán hết!)
                                // Lưu ý: Sửa lại chữ "Đăng nhập Hệ thống Đấu giá" cho khớp với Title bạn set ở hàm showLoginScreen()
                                if (!"Sàn Đấu Giá VIP PRO - Client".equals(stage.getTitle())) {
                                    stage.close();
                                }
                            }
                        }
                    });
                    break;
            }
        });
    }

    public void createAuctionCard(String roomId, String itemName, String sellerName) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefSize(180, 150);
        card.setAlignment(Pos.CENTER);

        Label lblName = new Label(itemName);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label lblId = new Label("Mã: " + roomId);

        Button btnJoin = new Button("Vào Phòng");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

        btnJoin.setOnAction(e -> {
            this.currentRoomId = roomId;
            auctionService.joinRoom(roomId);
        });

        card.getChildren().addAll(lblName, lblId, btnJoin);
        if (paneSelectAuction != null) paneSelectAuction.getChildren().add(card);
    }

    public void updateConnectionStatus(String status) {
        Platform.runLater(() -> { if (lblStatus != null) lblStatus.setText("Trạng thái: " + status); });
    }

    public void openLoginScreen(){
        showLoginScreen();
        Stage stage = (Stage) paneLogin.getScene().getWindow();
        stage.setMaximized(false);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.centerOnScreen();

        txtUsername.clear();
        txtPassword.clear();
        lblStatus.setText("Đã đăng xuất thành công.");
        lblStatus.setTextFill(Color.GREEN);
    }

    @FXML
    private void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seller-view.fxml"));
            Parent root = loader.load();

            SellerController sellerCtrl = loader.getController();
            sellerCtrl.setAuctionService(this.auctionService);

            Stage sellerStage = new Stage();
            sellerStage.setTitle("Tạo phiên đấu giá");
            sellerStage.setScene(new Scene(root));
            sellerStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-view.fxml"));
            Parent root = loader.load();

            this.adminController = loader.getController();
            this.adminController.setAuctionService(this.auctionService);

            Stage adminStage = new Stage();
            adminStage.setTitle("Hệ Thống Quản Trị - Admin Dashboard");
            adminStage.setScene(new Scene(root));
            adminStage.setMaximized(true);
            adminStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}