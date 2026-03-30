package client.controllers;

import client.services.AuctionService;
import client.services.ClientConnection;
import common.models.Auctions.AuctionRoom;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import common.DTO.Message;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Lớp AuctionController là Controller chính quản lý toàn bộ giao diện của Client.
 * Nhiệm vụ:
 * 1. Liên kết các thành phần giao diện (được định nghĩa trong file .fxml) thông qua annotation @FXML.
 * 2. Bắt các sự kiện (Click, Nhập text,...) từ người dùng và gọi AuctionService để gửi lên Server.
 * 3. Lắng nghe dữ liệu trả về từ Server (thông qua hàm onServerResponse) và cập nhật lên UI.
 * * * LƯU Ý ĐẶC BIỆT CHO NGƯỜI PHÁT TRIỂN SAU:
 * Mọi thao tác thay đổi giao diện (setText, setVisible, thêm Node...) ĐỀU PHẢI
 * ĐƯỢC ĐẶT TRONG `Platform.runLater()`. Nếu không, ứng dụng sẽ bị crash do xung đột luồng (Thread).
 */

public class AuctionController implements Initializable {
    // ==========================================================
    // KHAI BÁO CÁC THÀNH PHẦN GIAO DIỆN (@FXML)
    // ==========================================================
    // 1. Các Container (Màn hình chính) - Dùng để chuyển đổi qua lại
    @FXML private VBox paneLogin, paneSelectAuction, paneAuctionRoom, paneRegister, paneForgotPassword;

    // 2. Màn hình Đăng nhập / Đăng ký
    @FXML private TextField txtUsername,txtRegUsername,txtForgotUsername;
    @FXML private PasswordField txtPassword,txtRegPassword, txtRegConfirm,txtForgotNewPassword, txtForgotConfirm;
    @FXML private Label lblStatus,lblRegStatus,lblForgotStatus;
    @FXML private ComboBox<String> cbRegRole;

    // 3. Màn hình Chính (Dashboard - Danh sách phòng đấu giá)
    @FXML private FlowPane gridAuctions;

    @FXML private Button btnCreateAuction;

    // 4. Màn hình Trong phòng đấu giá
    @FXML private Label lblAuctionItemName,lblCurrentPrice, lblParticipantCount;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtBidAmount, txtChatInput;

    // ==========================================================
    // KHAI BÁO CÁC TẦNG DỊCH VỤ (SERVICES)
    // ==========================================================
    private ClientConnection clientConnection;
    private AuctionService auctionService;

    /**
     * Hàm này tự động chạy ngay sau khi file FXML được load lên.
     * Dùng để khởi tạo các giá trị mặc định, kết nối mạng.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logToScreen("");
        // Khởi tạo kết nối mạng và Service ngay từ đầu
        clientConnection = new ClientConnection(this);
        auctionService = new AuctionService(clientConnection);
        clientConnection.connect();
        if (cbRegRole != null) {
            cbRegRole.getItems().addAll("BIDDER", "SELLER");
            cbRegRole.setValue("BIDDER"); // Mặc định là Bidder
        }
    }

    /**
     * Hàm tiện ích giúp chuyển đổi giữa các màn hình (Ẩn tất cả, chỉ hiện màn hình được chọn).
     */
    //Switch Window
    @FXML
    private void showRegisterScreen() {
        switchScreen(paneRegister);
        lblRegStatus.setText("");
    }

    @FXML
    private void showLoginScreen() {
        switchScreen(paneLogin);
    }

    private void switchScreen(VBox screenToShow) {
        paneLogin.setVisible(false);
        paneRegister.setVisible(false);
        paneSelectAuction.setVisible(false);
        paneAuctionRoom.setVisible(false);
        if (paneForgotPassword != null) paneForgotPassword.setVisible(false);

        screenToShow.setVisible(true);
        screenToShow.toFront();
    }

    // ==========================================================
    // XỬ LÝ SỰ KIỆN TỪ GIAO DIỆN (USER ACTIONS)
    // ==========================================================
    //Event
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
    public void showForgotPasswordScreen(){
        switchScreen(paneForgotPassword);
        lblForgotStatus.setText("");
    }

    @FXML
    private void handleSubmitRegister() {
        String newRegUser = txtRegUsername.getText().trim();
        String newRegPass = txtRegPassword.getText().trim();
        String role = cbRegRole.getValue(); // Lấy vai trò

        // Gửi lên server. (Sếp nhớ vào AuctionService của Client để sửa hàm register
        // cho phép gửi thêm role đi nhé. VD: new Message("REGISTER", role, user+"|"+pass))
        auctionService.register(newRegUser, newRegPass, role);
    }

    @FXML
    private void handleSubmitForgotPassword() {
        String user = txtForgotUsername.getText().trim();
        String pass = txtForgotNewPassword.getText().trim();
        String confirm = txtForgotConfirm.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblForgotStatus.setText("Vui lòng nhập đủ thông tin!");
            return;
        }
        if (!pass.equals(confirm)) {
            lblForgotStatus.setText("Mật khẩu không khớp!");
            return;
        }

        // Gọi chuyên gia Service làm việc
        auctionService.resetPassword(user, pass);
    }

    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            auctionService.placeBid(amount);
            txtBidAmount.clear();
        } catch (NumberFormatException e) {
            txtChatLog.appendText("Vui lòng nhập số tiền hợp lệ!\n");
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

    // ==========================================================
    // NHẬN VÀ XỬ LÝ PHẢN HỒI TỪ SERVER (ROUTER)
    // ==========================================================

    /**
     * Đây là "Trái tim" của Controller. Mọi luồng dữ liệu Server gửi về đều phải đi qua đây.
     * @param msg Gói tin chứa kết quả xử lý từ Server.
     */
    public void onServerResponse(Message msg) {
        Platform.runLater(() -> {
            switch (msg.action) {
                case "LOGIN_SUCCESS":
                    // Lưu định danh người dùng
                    ClientConnection.currentUser = txtUsername.getText();
                    auctionService.setCurrentUser(txtUsername.getText().trim());

                    lblStatus.setText("✅ Đăng nhập thành công!");
                    lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);

                    // Phân quyền hiển thị UI: Nếu là Seller thì bật nút "+ Create Auction"
                    String userRole = msg.id;
                    if (userRole.equalsIgnoreCase("Seller")) {
                        btnCreateAuction.setVisible(true);
                        btnCreateAuction.setManaged(true);
                    } else {
                        btnCreateAuction.setVisible(false);
                        btnCreateAuction.setManaged(false);
                    }

                    // Lấy danh sách phòng và chuyển sang Màn hình chính
                    clientConnection.sendMessage(new Message("GET_ROOMS", ClientConnection.currentUser, ""));
                    switchScreen(paneSelectAuction);
                    break;

                case "LOGIN_FAIL":
                    lblStatus.setText("❌ " + msg.data);
                    lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                    break;

                case "REGISTER_SUCCESS":
                    lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                    showLoginScreen();
                    txtUsername.setText(txtRegUsername.getText());
                    txtPassword.clear();
                    break;

                case "REGISTER_FAIL":
                    lblRegStatus.setText("❌ " + msg.data);
                    break;

                case "ROOM_JOINED":
                    AuctionRoom joinedRoom = (AuctionRoom) msg.data;
                    lblAuctionItemName.setText(joinedRoom.getItemName());
                    lblCurrentPrice.setText("Giá hiện tại: " + joinedRoom.getCurrentPrice() + " $");
                    break;

                case "NEW_BID":
                    lblCurrentPrice.setText("Giá hiện tại: " + msg.data + " $");
                    txtChatLog.appendText("New price: [" + msg.id + "] has paid:" + msg.data + "$\n");
                    break;

                case "CHAT_MSG":
                    txtChatLog.appendText("[" + msg.id + "]: " + msg.data + "\n");
                    break;

                case "BID_FAIL":
                    txtChatLog.appendText("Hệ thống: " + msg.data + "\n");
                    break;

                case "RESET_SUCCESS":
                    lblStatus.setText("Đổi mật khẩu thành công!");
                    lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                    showLoginScreen();
                    txtUsername.setText(txtForgotUsername.getText());
                    txtPassword.clear();
                    break;

                case "RESET_FAIL":
                    lblForgotStatus.setText("❌ " + msg.data);
                    break;

                case "ROOM_LIST":
                    // ÉP BUỘC PHẢI BỌC TRONG PLATFORM.RUNLATER KHI VẼ GIAO DIỆN
                    javafx.application.Platform.runLater(() -> {
                        // 1. Xóa sạch danh sách phòng cũ trên màn hình
                        gridAuctions.getChildren().clear();

                        // 2. Tách chuỗi dữ liệu phòng mới và vẽ lại (Sếp giữ nguyên code cũ của sếp)
                        String[] rooms = ((String) msg.data).split(";");
                        for (String roomData : rooms) {
                            if (!roomData.isEmpty()) {
                                String[] info = roomData.split("\\|");
                                if (info.length >= 3) {
                                    createAuctionCard(info[0], info[1], info[2]); // Hàm vẽ thẻ phòng của sếp
                                }
                            }
                        }
                    });
                    break;
                // --- KHI SELLER TẠO PHÒNG THÀNH CÔNG ---
                case "CREATE_AUCTION_SUCCESS":
                    // Lấy mã phòng mà Server trả về
                    String autoJoinRoomId = msg.id;

                    // Gửi lệnh xin vào phòng lên Server
                    auctionService.joinRoom(autoJoinRoomId);

                    // Code liên quan đến Giao diện (UI) phải bọc trong Platform.runLater
                    javafx.application.Platform.runLater(() -> {
                        // 1. Chuyển sang giao diện phòng
                        switchScreen(paneAuctionRoom);

                        // 2. Bật lại cái cửa sổ chính đã bị giấu lúc nãy lên!
                        javafx.stage.Stage mainStage = (javafx.stage.Stage) paneAuctionRoom.getScene().getWindow();
                        if (!mainStage.isShowing()) {
                            mainStage.show();
                        }
                    });
                    break;

                // --- KHI CÓ PHÒNG MỚI (DÀNH CHO BIDDER) ---
                case "UPDATE_ROOMS":
                    System.out.println("🔄 Nhận được thông báo có phòng mới, đang tải lại...");
                    // Gọi hàm chuẩn từ Service
                    auctionService.getRooms();
                    break;
            }
        });
    }

    private void createAuctionCard(String roomId, String itemName, String sellerName) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefSize(180, 150);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        Label lblName = new Label(itemName);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label lblId = new Label("Mã: " + roomId);

        Button btnJoin = new Button("Vào Phòng");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

        btnJoin.setOnAction(e -> {
            auctionService.joinRoom(roomId);
            switchScreen(paneAuctionRoom);
        });

        card.getChildren().addAll(lblName, lblId, btnJoin);
        gridAuctions.getChildren().add(card);
    }

    private void logToScreen(String text) {
        txtChatLog.appendText(text + "\n");
    }

    /**
     * Cập nhật trạng thái mạng (Đang kết nối, Mất kết nối...)
     */
    public void updateConnectionStatus(String status) {
        Platform.runLater(() -> lblStatus.setText("Trạng thái: " + status));
    }

    /**
     * Hàm mở giao diện tạo phiên đấu giá (Chỉ dành cho Seller).
     * Mở dưới dạng Pop-up (Cửa sổ phụ) đè lên giao diện chính.
     */
    @FXML
    private void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/seller-view.fxml"));
            javafx.scene.Parent root = loader.load();

            // Lấy Controller của màn hình Seller và truyền Service qua cho nó dùng chung
            SellerController sellerCtrl = loader.getController();
            sellerCtrl.setAuctionService(this.auctionService);

            javafx.stage.Stage sellerStage = new javafx.stage.Stage();
            sellerStage.setTitle("Tạo phiên đấu giá (Seller: " + ClientConnection.currentUser + ")");
            sellerStage.setScene(new javafx.scene.Scene(root));
            sellerStage.show();

            // XÓA HOẶC COMMENT 2 DÒNG NÀY (Để không bị mất Màn hình chính phía sau)
            // javafx.stage.Stage currentStage = (javafx.stage.Stage) paneLogin.getScene().getWindow();
            // currentStage.hide();

            // Ghi chú: Cố tình KHÔNG đóng màn hình hiện tại (paneSelectAuction)
            // để Seller vẫn thấy danh sách phòng sau khi tạo xong.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm hỗ trợ vẽ danh sách phòng đấu giá lên màn hình chính.(Chưa dùng đến)
     */
    private void renderRoomList(String roomsData) {}
}