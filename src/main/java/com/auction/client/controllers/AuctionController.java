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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    @FXML private Label lblCountdownTimer;
    @FXML private Button btnCloseAuction, btnCreateAuction;

    @FXML private FlowPane paneSelectAuction; // Container chứa các phòng (Cột giữa)

    @FXML private Label lblAuctionItemName, lblCurrentPrice, lblParticipantCount,lblTimer;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtBidAmount, txtChatInput;

    @FXML private BorderPane paneMainLobby;
    @FXML private VBox vboxCurrencyRates, vboxNews;
    @FXML private Button btnPlaceBid;
    @FXML private TextArea txtItemDescriptionDisplay;

    // ==========================================================
    // KHAI BÁO CÁC TẦNG DỊCH VỤ (SERVICES)
    // ==========================================================
    private ClientConnection clientConnection;
    private AuctionService auctionService;
    private String currentRoomId;
    private String myUsername = "";
    private User currentUserProfile;
    private AdminController adminController;
    private Timeline auctionTimer;
    private AuctionRoom currentRoom;
    private boolean isNetworkConnected = false;

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
    public void handleLogout() {
        // Có thể thêm log báo hiệu
        System.out.println("Đang thực hiện đăng xuất...");

        // Gọi lại hàm openLoginScreen an toàn mà chúng ta vừa sửa ở bước trước
        openLoginScreen();
    }

    @FXML
    public void openLoginScreen() {
        try {
            resetSessionState();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/login-view.fxml"));
            loader.setControllerFactory(clazz -> this);
            Parent root = loader.load();

            Stage stage = getSafeWindow();
            stage.setScene(new Scene(root));
            configureStage(stage, "Sàn Đấu Giá", false, 800, 600);

            if (txtUsername != null) txtUsername.clear();
            if (txtPassword != null) txtPassword.clear();
            if (lblStatus != null) {
                lblStatus.setText("✅ Đã đăng xuất thành công.");
                lblStatus.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tải lại màn hình Đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
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
            if (currentRoom != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                if (currentRoom.getStartTime() != null && now.isBefore(currentRoom.getStartTime())) {
                    txtChatLog.appendText("⚠️ Phiên đấu giá chưa bắt đầu, chưa thể đặt giá!\n");
                    return;
                }

                if (currentRoom.getActualEndTime() != null && !now.isBefore(currentRoom.getActualEndTime())) {
                    txtChatLog.appendText("⚠️ Phiên đấu giá đã hết giờ, không thể đặt giá!\n");
                    disableBidUI("Phiên đấu giá đã hết giờ.");
                    return;
                }
            }

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
    public void handleBackToSelection() {
        // Tắt đồng hồ
        if (auctionTimer != null) {
            auctionTimer.stop();
        }

        // Gửi lệnh rời phòng cho Server
        auctionService.leaveRoom();

        //Dọn dẹp data cũ
        this.currentRoom = null;
        this.currentRoomId = null;

        if (btnCloseAuction != null) {
            btnCloseAuction.setVisible(false);
            btnCloseAuction.setManaged(false);
        }

        showMainLobby();

        auctionService.getRooms();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. 🔥 BỌC THÉP: CHỈ KẾT NỐI MẠNG ĐÚNG 1 LẦN DUY NHẤT
        if (!isNetworkConnected) {
            clientConnection = new ClientConnection(this);
            auctionService = new AuctionService(clientConnection);
            clientConnection.connect();

            isNetworkConnected = true; // Đánh dấu là đã kết nối
            System.out.println("✅ Đã khởi tạo mạng thành công (Chỉ chạy 1 lần)");
        } else {
            System.out.println("⚠️ Bỏ qua kết nối mạng (Sử dụng lại kết nối cũ)");
        }

        // 2. Nạp dữ liệu cho giao diện (Phần này được phép chạy nhiều lần)
        if (cbRegRole != null && cbRegRole.getItems().isEmpty()) {
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
        try {
            if (auctionTimer != null) {
                auctionTimer.stop();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/mainLobby-view.fxml"));
            // (Nhớ kiểm tra lại đường dẫn /views/ hoặc bỏ đi nếu file của bạn ở ngoài nhé)

            loader.setControllerFactory(clazz -> this);
            Parent root = loader.load();

            // Ép trạng thái mặc định an toàn trước khi đổ dữ liệu role mới
            if (btnCreateAuction != null) {
                btnCreateAuction.setVisible(false);
                btnCreateAuction.setManaged(false);
            }

            if (btnCloseAuction != null) {
                btnCloseAuction.setVisible(false);
                btnCloseAuction.setManaged(false);
            }

            // 1. Dùng hàm lấy cửa sổ
            Stage stage = getSafeWindow();

            // 2. 🛡️ BACKUP BẮT BUỘC: Nếu vẫn null thì quét toàn bộ cửa sổ đang mở trên màn hình
            if (stage == null) {
                stage = (Stage) javafx.stage.Window.getWindows().stream()
                        .filter(javafx.stage.Window::isShowing)
                        .findFirst()
                        .orElse(null);
            }

            // 3. Đè giao diện Sảnh chính lên
            if (stage != null) {
                stage.setScene(new Scene(root));
                configureStage(stage, "Sảnh Chính - Hệ Thống Đấu Giá", true, 0, 0);
            } else {
                System.out.println("❌ LỖI TỘT ĐỘ: Không tìm thấy bất kỳ cửa sổ nào đang mở!");
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tải Sảnh chính: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAuctionRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/auction-view.fxml"));
            loader.setControllerFactory(clazz -> this);
            Parent root = loader.load();

            Stage stage = getSafeWindow();

            if (stage == null) {
                stage = (Stage) javafx.stage.Window.getWindows().stream()
                        .filter(javafx.stage.Window::isShowing)
                        .findFirst()
                        .orElse(null);
            }

            if (stage != null) {
                stage.setScene(new Scene(root));
                configureStage(stage, "Sàn Đấu Giá - Trong phòng", true, 0, 0);
            } else {
                System.out.println("❌ LỖI: Không tìm thấy cửa sổ để hiển thị phòng đấu giá!");
            }

            if (this.currentRoom != null) {
                if (txtChatLog != null) {
                    txtChatLog.clear();
                }

                if (txtItemDescriptionDisplay != null) {
                    String description = this.currentRoom.getItemDescription();
                    txtItemDescriptionDisplay.setText(
                            description != null && !description.isBlank()
                                    ? description
                                    : "Chưa có mô tả cho vật phẩm này."
                    );
                }

                updateAuctionTimeUI(this.currentRoom);
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi tải Phòng đấu giá: " + e.getMessage());
            e.printStackTrace();
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
                    System.out.println("Role nhận được từ Server: " + loggedInUser.getRole());

                    // 1. Cập nhật dữ liệu ngầm (Không đụng chạm UI nên để thoải mái ở ngoài)
                    this.currentUserProfile = loggedInUser;
                    String userId = loggedInUser.getId();
                    ClientConnection.currentUser = userId;
                    auctionService.setCurrentUser(userId);
                    this.myUsername = loggedInUser.getUsername();

                    // 2. MỌI THAO TÁC CẬP NHẬT GIAO DIỆN BẮT BUỘC PHẢI BỌC TRONG Platform.runLater
                    Platform.runLater(() -> {
                        // Báo thành công trên màn hình Login
                        if (lblStatus != null) {
                            lblStatus.setText("✅ Đăng nhập thành công!");
                            lblStatus.setTextFill(Color.GREEN);
                        }

                        // 3. PHÂN LUỒNG ROLE
                        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                            // NẾU LÀ ADMIN -> MỞ CỬA SỔ QUẢN TRỊ ĐỘC LẬP
                            openAdminDashboard();

                            // Ẩn cửa sổ đăng nhập hiện tại đi
                            if (paneLogin != null && paneLogin.getScene() != null) {
                                paneLogin.getScene().getWindow().hide();
                            }

                        } else {
                            // NẾU LÀ USER BÌNH THƯỜNG -> VÀO SẢNH
                            // ⚠️ CHÚ Ý: Phải gọi showMainLobby() TRƯỚC để JavaFX nạp các biến @FXML của sảnh chính
                            showMainLobby();

                            // SAU KHI nạp xong sảnh, mới bắt đầu đổ chữ vào các Label
                            if (lblUsername != null) lblUsername.setText("Xin chào: " + loggedInUser.getUsername());
                            if (lblBalance != null) lblBalance.setText("Số dư ví: " + String.format("%,.0f $", loggedInUser.getBalance()));

                            // Phân quyền hiện nút "Tạo phiên đấu giá" cho Seller
                            boolean isSeller = "SELLER".equalsIgnoreCase(loggedInUser.getRole());

                            if (btnCreateAuction != null) {
                                btnCreateAuction.setVisible(false);
                                btnCreateAuction.setManaged(false);

                                if (isSeller) {
                                    btnCreateAuction.setVisible(true);
                                    btnCreateAuction.setManaged(true);
                                }
                            }

                            // Gửi yêu cầu lấy danh sách phòng chờ lên Server
                            clientConnection.sendMessage(new Message("GET_ROOMS", userId, ""));
                        }
                    });
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
                    this.currentRoom = room;
                    this.currentRoomId = room.getRoomId();

                    showAuctionRoom();

                    if (lblAuctionItemName != null) lblAuctionItemName.setText(room.getItemName());
                    if (lblCurrentPrice != null) lblCurrentPrice.setText("Giá hiện tại: " + String.format("%,.0f $", room.getCurrentPrice()));

                    if (txtItemDescriptionDisplay != null) {
                        String description = room.getItemDescription();
                        txtItemDescriptionDisplay.setText(
                                description != null && !description.isBlank()
                                        ? description
                                        : "Chưa có mô tả cho vật phẩm này."
                        );
                    }

                    boolean isOwner = room.getNameSeller() != null && room.getNameSeller().trim().equalsIgnoreCase(myUsername.trim());
                    if (btnCloseAuction != null) {
                        btnCloseAuction.setVisible(isOwner);
                        btnCloseAuction.setManaged(isOwner);
                    }

                    updateAuctionTimeUI(room);
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
                    if (txtChatLog != null) {
                        txtChatLog.appendText("❌ " + msg.data + "\n");
                    }
                    if (lblStatus != null && msg.data != null) {
                        lblStatus.setText("❌ " + msg.data);
                        lblStatus.setTextFill(Color.RED);
                    }
                    break;

                case "ROOM_FAIL":
                    Alert roomFailAlert = new Alert(Alert.AlertType.WARNING, String.valueOf(msg.data));
                    roomFailAlert.setHeaderText("Không thể vào phòng");
                    roomFailAlert.showAndWait();
                    showMainLobby();
                    this.currentRoom = null;
                    this.currentRoomId = null;
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

    @FXML
    private void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/auctionprototype/seller-view.fxml"));
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
            /*Nếu chuyển đường dẫn admin-view thì thêm đường link này /com/example/auctionprototype/*/
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

    private Stage getSafeWindow() {
        if (paneLogin != null && paneLogin.getScene() != null) return (Stage) paneLogin.getScene().getWindow();
        if (paneMainLobby != null && paneMainLobby.getScene() != null) return (Stage) paneMainLobby.getScene().getWindow();
        if (paneAuctionRoom != null && paneAuctionRoom.getScene() != null) return (Stage) paneAuctionRoom.getScene().getWindow();
        return (Stage) javafx.stage.Window.getWindows().get(0);
    }

    // Hàm đếm ngược
    private void startCountdown(java.time.LocalDateTime endTime) {
        if (auctionTimer != null) auctionTimer.stop();

        auctionTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    long secondsLeft = java.time.temporal.ChronoUnit.SECONDS.between(java.time.LocalDateTime.now(), endTime);

                    if (secondsLeft <= 0) {
                        lblTimer.setText("⏱️ Đã kết thúc!");
                        lblTimer.setTextFill(javafx.scene.paint.Color.RED);

                        if (btnPlaceBid != null) {
                            btnPlaceBid.setDisable(true);
                        }

                        auctionTimer.stop();
                    } else {
                        long hh = secondsLeft / 3600;
                        long mm = (secondsLeft % 3600) / 60;
                        long ss = secondsLeft % 60;
                        lblTimer.setText(String.format("⏱️ %02d:%02d:%02d", hh, mm, ss));
                        lblTimer.setTextFill(javafx.scene.paint.Color.DARKGREEN);

                        if (btnPlaceBid != null) {
                            btnPlaceBid.setDisable(false);
                        }
                    }
                })
        );

        auctionTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        auctionTimer.play();
    }

    private void updateAuctionTimeUI(AuctionRoom room) {
        if (room == null || lblTimer == null) return;

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startTime = room.getStartTime();
        java.time.LocalDateTime endTime = room.getActualEndTime();

        if (auctionTimer != null) {
            auctionTimer.stop();
        }

        // Chưa có thời gian rõ ràng
        if (startTime == null || endTime == null) {
            lblTimer.setText("Vô thời hạn");
            lblTimer.setTextFill(javafx.scene.paint.Color.ORANGE);
            if (btnPlaceBid != null) btnPlaceBid.setDisable(false);
            return;
        }

        // Chưa bắt đầu
        if (now.isBefore(startTime)) {
            long secondsToStart = java.time.temporal.ChronoUnit.SECONDS.between(now, startTime);

            if (btnPlaceBid != null) btnPlaceBid.setDisable(true);

            auctionTimer = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                        long left = java.time.temporal.ChronoUnit.SECONDS.between(java.time.LocalDateTime.now(), startTime);

                        if (left <= 0) {
                            updateAuctionTimeUI(room);
                        } else {
                            long hh = left / 3600;
                            long mm = (left % 3600) / 60;
                            long ss = left % 60;
                            lblTimer.setText(String.format("🕒 Chưa bắt đầu: %02d:%02d:%02d", hh, mm, ss));
                            lblTimer.setTextFill(javafx.scene.paint.Color.BLUE);
                        }
                    })
            );
            auctionTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
            auctionTimer.play();
            return;
        }

        // Đã hết giờ
        if (!now.isBefore(endTime)) {
            lblTimer.setText("⏱️ Đã kết thúc!");
            lblTimer.setTextFill(javafx.scene.paint.Color.RED);
            if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
            return;
        }

        // Đang diễn ra
        startCountdown(endTime);
    }

    private void disableBidUI(String reason) {
        if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
        if (txtBidAmount != null) txtBidAmount.setDisable(true);

        if (txtChatLog != null && reason != null && !reason.isBlank()) {
            txtChatLog.appendText("⚠️ " + reason + "\n");
        }
    }

    private void resetSessionState() {
        // Reset dữ liệu nghiệp vụ
        this.currentRoomId = null;
        this.currentRoom = null;
        this.currentUserProfile = null;
        this.myUsername = "";
        this.adminController = null;

        ClientConnection.currentUser = null;
        if (this.auctionService != null) {
            this.auctionService.setCurrentUser(null);
        }

        // Dừng timer nếu còn chạy
        if (auctionTimer != null) {
            auctionTimer.stop();
            auctionTimer = null;
        }

        // Reset UI nếu control đã được inject
        if (btnCreateAuction != null) {
            btnCreateAuction.setVisible(false);
            btnCreateAuction.setManaged(false);
        }

        if (btnCloseAuction != null) {
            btnCloseAuction.setVisible(false);
            btnCloseAuction.setManaged(false);
        }

        if (btnPlaceBid != null) {
            btnPlaceBid.setDisable(false);
        }

        if (txtBidAmount != null) {
            txtBidAmount.clear();
            txtBidAmount.setDisable(false);
        }

        if (txtChatInput != null) {
            txtChatInput.clear();
            txtChatInput.setDisable(false);
        }

        if (txtChatLog != null) {
            txtChatLog.clear();
        }

        if (lblUsername != null) {
            lblUsername.setText("");
        }

        if (lblBalance != null) {
            lblBalance.setText("");
        }

        if (lblAuctionItemName != null) {
            lblAuctionItemName.setText("");
        }

        if (lblCurrentPrice != null) {
            lblCurrentPrice.setText("");
        }

        if (lblTimer != null) {
            lblTimer.setText("");
        }
    }

    private void configureStage(Stage stage, String title, boolean maximized, double width, double height) {
        if (stage == null) return;

        stage.setTitle(title);
        stage.setMaximized(maximized);

        if (!maximized) {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
        }
    }
}