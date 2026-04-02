package com.auction.client.controllers;

import com.auction.server.service.AuctionService;
import com.auction.server.service.ClientConnection;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Seller;
import com.auction.common.model.User;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.auction.common.dto.Message;
import javafx.fxml.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.auction.server.dao.MockDB;

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
    @FXML private Label lblUsername;
    @FXML private Label lblBalance;

    @FXML private Button btnCloseAuction;

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
    private String currentRoomId;
    private String myUsername = "";
    private User currentUserProfile;

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
            txtChatLog.appendText("❌ Vui lòng nhập số tiền hợp lệ!\n");
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

    @FXML
    private void handleCloseAuction() {
        if (this.currentRoomId != null) {
            // 1. Gửi lệnh chốt đơn lên Server để xử lý trừ tiền, cộng tiền
            auctionService.closeAuction(this.currentRoomId);

            // 2. Hiển thị thông báo Pop-up cho ngầu
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chốt đơn thành công");
            alert.setHeaderText(null);
            alert.setContentText("Phòng đấu giá đã được đóng. Chúc mừng bạn đã bán thành công!");
            alert.showAndWait();

            // 3. Đóng giao diện phòng đấu giá và bật lại giao diện Sảnh chính
            paneAuctionRoom.setVisible(false);
            paneSelectAuction.setVisible(true);

            // 4. Reset lại ID phòng hiện tại
            this.currentRoomId = null;
        }
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
                    Platform.runLater(() -> {
                        // 1. Ép kiểu dữ liệu Server gửi về thành object User (Class cha của Bidder và Seller)
                        User loggedInUser = (User) msg.data;

                        // 🌟 LƯU LẠI PROFILE ĐỂ DÙNG XUYÊN SUỐT
                        this.currentUserProfile = loggedInUser;

                        // 2. LƯU ĐÚNG ĐỊNH DANH (ID) người dùng để dùng cho các luồng Chat/Bid sau này
                        String userId = loggedInUser.getId();
                        ClientConnection.currentUser = userId;
                        auctionService.setCurrentUser(userId);

                        this.myUsername = loggedInUser.getUsername();

                        // 3. Cập nhật thông tin lên giao diện
                        lblUsername.setText("Xin chào: " + loggedInUser.getUsername());
                        lblBalance.setText("Số dư ví: " + String.format("%,.0f $", loggedInUser.getBalance()));

                        lblStatus.setText("✅ Đăng nhập thành công!");
                        lblStatus.setTextFill(Color.GREEN);

                        // 4. Phân quyền hiển thị UI: Bật nút "+ Create Auction" cho Seller
                        boolean isSeller = false;

                        // Cách 1: Kiểm tra xem object gửi về có phải là class Seller không (Dành cho user tạo qua AuthService)
                        if (loggedInUser instanceof Seller) {
                            isSeller = true;
                        }
                        // Cách 2: Kiểm tra qua role trong Message nếu Server có gửi (Dành cho user khởi tạo cứng trong MockDB)
                        else if (msg.role != null && msg.role.equalsIgnoreCase("SELLER")) {
                            isSeller = true;
                        }

                        // Bật/tắt nút dựa trên kết quả
                        if (isSeller) {
                            btnCreateAuction.setVisible(true);
                            btnCreateAuction.setManaged(true);
                        } else {
                            btnCreateAuction.setVisible(false);
                            btnCreateAuction.setManaged(false);
                        }

                        // 5. Gửi lệnh xin danh sách phòng lên Server (dùng userId chuẩn)
                        clientConnection.sendMessage(new Message("GET_ROOMS", userId, ""));

                        // 6. Chuyển sang Màn hình chính
                        switchScreen(paneSelectAuction);
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


                case "ROOM_JOINED":
                    AuctionRoom room = (AuctionRoom) msg.data;
                    this.currentRoomId = room.getRoomId();

                    Platform.runLater(() -> {
                        lblAuctionItemName.setText(room.getItemName());
                        lblCurrentPrice.setText("Giá hiện tại: " + room.getCurrentPrice() + " $");

                        String sellerNameOfRoom = room.getNameSeller();
                        String myName = this.myUsername;

                        System.out.println("=== KIỂM TRA DỮ LIỆU PHÒNG ===");
                        System.out.println("1. Tên Sản Phẩm thực tế: [" + room.getItemName() + "]");
                        System.out.println("2. Chủ phòng lưu trên Server: [" + sellerNameOfRoom + "]");
                        System.out.println("3. Tên tôi đang đăng nhập: [" + myName + "]");
                        System.out.println("==============================");

                        // So sánh chuẩn Tên vs Tên
                        boolean isOwner = false;
                        if (sellerNameOfRoom != null && myUsername != null) {
                            isOwner = sellerNameOfRoom.trim().equalsIgnoreCase(myUsername.trim());
                        }

                        btnCloseAuction.setVisible(isOwner);
                        btnCloseAuction.setManaged(isOwner);

                        paneSelectAuction.setVisible(false);
                        paneAuctionRoom.setVisible(true);
                    });
                    break;

                case "CHAT_MSG":
                    txtChatLog.appendText("[" + this.myUsername + "]: " + msg.data + "\n");
                    break;

                case "RESET_SUCCESS":
                    lblStatus.setText("Đổi mật khẩu thành công!");
                    lblStatus.setTextFill(Color.GREEN);
                    showLoginScreen();
                    txtUsername.setText(txtForgotUsername.getText());
                    txtPassword.clear();
                    break;

                case "RESET_FAIL":
                    lblForgotStatus.setText("❌ " + msg.data);
                    break;

                case "ROOM_LIST":
                    // ÉP BUỘC PHẢI BỌC TRONG PLATFORM.RUNLATER KHI VẼ GIAO DIỆN
                    Platform.runLater(() -> {
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
                    currentRoomId = autoJoinRoomId;

                    // Code liên quan đến Giao diện (UI) phải bọc trong Platform.runLater
                    Platform.runLater(() -> {
                        // 1. Chuyển sang giao diện phòng
                        switchScreen(paneAuctionRoom);

                        // 2. Bật lại cái cửa sổ chính đã bị giấu lúc nãy lên!
                        Stage mainStage = (Stage) paneAuctionRoom.getScene().getWindow();
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
                case "BID_FAIL":
                    Platform.runLater(() -> txtChatLog.appendText("❌ " + msg.data + "\n"));
                    break;

                case "BID_SUCCESS":
                    Platform.runLater(() -> txtChatLog.appendText("✅ Đặt giá thành công!\n"));
                    break;

                case "AUCTION_CLOSED":
                    Platform.runLater(() -> {
                        // Nếu Client đang ở trong chính cái phòng vừa bị đóng
                        String closedRoomId = (String) msg.data;
                        if (this.currentRoomId != null && this.currentRoomId.equals(closedRoomId)) {

                            // Thông báo cho người mua biết phòng đã đóng
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Phiên đấu giá kết thúc");
                            alert.setHeaderText(null);
                            alert.setContentText("Chủ phòng đã chốt đơn! Bạn sẽ được đưa về sảnh chính.");
                            alert.showAndWait();

                            // Đá người mua về sảnh
                            paneAuctionRoom.setVisible(false);
                            paneSelectAuction.setVisible(true);
                            this.currentRoomId = null;
                        }
                    });
                    break;

                case "UPDATE_PRICE":
                    Platform.runLater(() -> {
                        // Tách chuỗi Server phát xuống
                        String[] parts = ((String) msg.data).split("\\|");
                        String bRoomId = parts[0];
                        String newPrice = parts[1];
                        String winner = parts[2];
                        System.out.println("Check dữ liệu: Cập nhật ngươời chiến thắng");
                        System.out.println("parts[0]"+parts[0]);
                        System.out.println("parts[1]"+parts[1]);
                        System.out.println("parts[2]"+parts[2]);

                        // GIẢ SỬ bạn có biến đang lưu phòng hiện tại, ví dụ: this.currentRoom
                        // Phải check xem người dùng đang mở đúng cửa sổ phòng đó không thì mới cập nhật Label
                        if (this.currentRoomId != null && this.currentRoomId.equals(bRoomId)) {
                            // Cập nhật Nhãn tiền to nhất trên màn hình (thay tên biến lblCurrentPrice bằng đúng id FXML của bạn)
                            lblCurrentPrice.setText(newPrice + " $");
                            txtChatLog.appendText("📢 New price:" + MockDB.userTable.get(winner).getUsername() + " has paid " + newPrice + "$\n");
                        }
                    });
                    break;

                case "UPDATE_BALANCE":
                    String targetUserId = msg.id; // ID của người có biến động số dư
                    double newBalance = (Double) msg.data; // Số dư mới nhất

                    // Kiểm tra xem tin báo này có phải dành cho tài khoản mình đang đăng nhập không
                    if (targetUserId.equals(auctionService.getCurrentUser())) {
                        Platform.runLater(() -> {
                            // 1. Nếu bạn có 1 cái Label hiển thị số dư ở màn hình chính, cập nhật nó ở đây
                            // Ví dụ: lblBalance.setText("Số dư: " + newBalance + " $");
                            if (this.currentUserProfile != null) {
                                this.currentUserProfile.setBalance(newBalance);
                            }

                            // UI CẬP NHẬT LUÔN TRÊN GÓC MÀN HÌNH:
                            lblBalance.setText("Số dư: " + String.format("%,.0f $", newBalance));

                            // 2. Thông báo Pop-up cho người dùng
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Biến động số dư");
                            alert.setHeaderText("Giao dịch đấu giá hoàn tất!");
                            alert.setContentText("Số dư hiện tại của bạn là: " + String.format("%,.0f $", newBalance));
                            alert.show();
                        });
                    }
                    break;

                case "AUCTION_CLOSED_NOTIFY":
                    Platform.runLater(() -> {
                        String closedRoomId = (String) msg.data;

                        // Nếu mình đang ở trong đúng cái phòng vừa bị chốt
                        if (this.currentRoomId != null && this.currentRoomId.equals(closedRoomId)) {

                            // 1. Thông báo cho người dùng
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Thông báo");
                            alert.setHeaderText("Phiên đấu giá đã kết thúc!");
                            alert.setContentText("Chủ phòng đã chốt đơn thành công. Bạn sẽ được đưa về sảnh.");
                            alert.showAndWait();

                            // 2. Tự động chuyển về màn hình danh sách phòng
                            paneAuctionRoom.setVisible(false);
                            paneSelectAuction.setVisible(true);

                            // 3. Xóa ID phòng hiện tại để tránh lỗi
                            this.currentRoomId = null;

                            // 4. (Tùy chọn) Gọi server để cập nhật lại danh sách phòng mới (đã xóa phòng cũ)
                            //auctionService.getAuctionRooms();
                        }
                    });
                    break;
            }
        });
    }

    private void createAuctionCard(String roomId, String itemName, String sellerName) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.auction.client.views/seller-view.fxml"));
            Parent root = loader.load();

            SellerController sellerCtrl = loader.getController();
            sellerCtrl.setAuctionService(this.auctionService);

            if (this.currentUserProfile instanceof Seller) {
                sellerCtrl.setSellerData((Seller) this.currentUserProfile);
            }

            Stage sellerStage = new Stage();
            sellerStage.setTitle("Tạo phiên đấu giá (Seller: " + ClientConnection.currentUser + ")");
            sellerStage.setScene(new Scene(root));
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