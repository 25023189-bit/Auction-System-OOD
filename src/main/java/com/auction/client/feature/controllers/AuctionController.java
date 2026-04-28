package com.auction.client.feature.controllers;

// Import rõ ràng, bỏ dấu * để tránh lỗi "ambiguous reference"
import com.auction.client.feature.auth.AuthPresenter;
import com.auction.client.feature.auth.LoginCommand;
import com.auction.client.feature.auth.LoginFormValidator;
import com.auction.client.feature.auth.RegisterCommand;
import com.auction.client.feature.auth.RegisterForm;
import com.auction.client.feature.auth.RegisterFormValidator;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.core.navigation.FxSceneNavigator;
import com.auction.client.session.SessionStore;
import com.auction.client.session.InMemorySessionStore;

import com.auction.client.feature.action.BidActionHandler;
import com.auction.client.feature.action.ChatActionHandler;
import com.auction.client.feature.action.AuctionCloseHandler;
import com.auction.client.feature.action.RoomTransitionHandler;

import com.auction.client.feature.presenter.AuctionLobbyPresenter;
import com.auction.client.feature.presenter.AuctionTimerService;
import com.auction.client.feature.presenter.LobbyUserInfoBinder;

import com.auction.client.feature.presenter.AuctionRoomPresenter;

import com.auction.client.feature.viewmodel.AuctionViewModel;
import com.auction.client.feature.viewmodel.AuthViewModel;

import com.auction.server.service.AuctionService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AuctionController {

    // ============ FXML COMPONENTS (AUTO-INJECT) ============
    @FXML private VBox paneLogin;
    @FXML private VBox paneRegister;
    @FXML private VBox paneForgotPassword;

    // ============ LOGIN COMPONENTS ============
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    // ============ REGISTER COMPONENTS ============
    @FXML private TextField txtRegCustomerId;
    @FXML private TextField txtRegUsername;
    @FXML private TextField txtRegEmail;
    @FXML private TextField txtRegFullName;
    @FXML private PasswordField txtRegPassword;
    @FXML private PasswordField txtRegConfirm;
    @FXML private ComboBox<String> cbRegRole;
    @FXML private Label lblRegOrganization;
    @FXML private TextField txtRegOrganization;
    @FXML private Label lblRegStatus;

    // ============ FORGOT PASSWORD COMPONENTS ============
    @FXML private TextField txtForgotUsername;
    @FXML private PasswordField txtForgotNewPassword;
    @FXML private PasswordField txtForgotConfirm;
    @FXML private Label lblForgotStatus;

    // ============ AUCTION ROOM COMPONENTS ============
    @FXML private VBox paneAuctionLobby;
    @FXML private VBox paneAuctionRoom;
    @FXML private Label lblRoomId;
    @FXML private Label lblItemName;
    @FXML private Label lblItemPrice;
    @FXML private Label lblCurrentBid;
    @FXML private Label lblTimeRemaining;
    @FXML private TextField txtBidAmount;
    @FXML private TextField txtChatInput;
    @FXML private TextArea txtChatDisplay;
    @FXML private ListView<String> lstBidHistory;
    @FXML private ListView<String> lstUsers;
    @FXML private ListView<String> lstAvailableRooms;
    @FXML private Label lblUserInfo;

    // ============ SERVICES & PRESENTERS ============
    private AuctionService auctionService;
    private SceneNavigator sceneNavigator;
    private SessionStore sessionStore;
    private AuthPresenter authPresenter;
    private AuctionRoomPresenter auctionRoomPresenter;
    private AuctionLobbyPresenter auctionLobbyPresenter;
    private AuctionTimerService auctionTimerService;
    private LobbyUserInfoBinder lobbyUserInfoBinder;

    // ============ VIEW MODELS ============
    private AuthViewModel authViewModel;
    private AuctionViewModel auctionViewModel;

    // ============ ACTION HANDLERS ============
    private BidActionHandler bidActionHandler;
    private ChatActionHandler chatActionHandler;
    private AuctionCloseHandler auctionCloseHandler;
    private RoomTransitionHandler roomTransitionHandler;

    @FXML
    public void initialize() {
        // Khởi tạo các Service và Store trước
        auctionService = new AuctionService(null);
        sessionStore = new InMemorySessionStore();

        // ĐÃ FIX: Truyền đủ 4 tham số vào FxSceneNavigator (this, windowStateHandler, sessionStore, auctionService)
        // Tạm truyền null cho WindowStateHandler, các tham số khác đã có
        sceneNavigator = new FxSceneNavigator(this, null, sessionStore, auctionService);

        authPresenter = new AuthPresenter(
                lblStatus, lblRegStatus, lblForgotStatus,
                txtUsername, txtPassword,
                txtForgotUsername, txtForgotNewPassword, txtForgotConfirm
        );

        // Setup AuctionRoomPresenter với đúng 10 tham số giao diện
        auctionRoomPresenter = new AuctionRoomPresenter(
                lblItemName,           // lblAuctionItemName
                lblCurrentBid,         // lblCurrentPrice
                lblTimeRemaining,      // lblTimer
                null,                  // lblParticipantCount
                lblItemPrice,          // lblDescription
                txtChatDisplay,        // txtChatLog
                null,                  // txtItemDescriptionDisplay
                null,                  // btnCloseAuction
                null,                  // btnPlaceBid
                txtBidAmount           // txtBidAmount
        );

        auctionLobbyPresenter = new AuctionLobbyPresenter();
        auctionTimerService = new AuctionTimerService();
        lobbyUserInfoBinder = new LobbyUserInfoBinder();

        authViewModel = new AuthViewModel();
        auctionViewModel = new AuctionViewModel();

        // Setup register role combo
        if (cbRegRole != null) {
            cbRegRole.getItems().addAll("BIDDER", "SELLER");
            cbRegRole.setValue("BIDDER");
            cbRegRole.setOnAction(e -> updateOrganizationVisibility());
        }
    }

    private void updateOrganizationVisibility() {
        String selectedRole = cbRegRole.getValue();
        boolean isSeller = "SELLER".equalsIgnoreCase(selectedRole);

        if (lblRegOrganization != null) lblRegOrganization.setVisible(isSeller);
        if (lblRegOrganization != null) lblRegOrganization.setManaged(isSeller);
        if (txtRegOrganization != null) txtRegOrganization.setVisible(isSeller);
        if (txtRegOrganization != null) txtRegOrganization.setManaged(isSeller);
    }

    @FXML
    private void showLoginScreen() {
        hideAllPanes();
        if (paneLogin != null) paneLogin.setVisible(true);
    }

    @FXML
    private void showRegisterScreen() {
        hideAllPanes();
        if (paneRegister != null) paneRegister.setVisible(true);
        updateOrganizationVisibility();
    }

    @FXML
    private void showForgotPasswordScreen() {
        hideAllPanes();
        if (paneForgotPassword != null) paneForgotPassword.setVisible(true);
        if (lblForgotStatus != null) lblForgotStatus.setText("");
    }

    private void hideAllPanes() {
        if (paneLogin != null) paneLogin.setVisible(false);
        if (paneRegister != null) paneRegister.setVisible(false);
        if (paneForgotPassword != null) paneForgotPassword.setVisible(false);
        if (paneAuctionLobby != null) paneAuctionLobby.setVisible(false);
        if (paneAuctionRoom != null) paneAuctionRoom.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        if (authViewModel == null) {
            authViewModel = new AuthViewModel();
        }

        authViewModel.setUsername(txtUsername != null ? txtUsername.getText() : "");
        authViewModel.setPassword(txtPassword != null ? txtPassword.getText() : "");

        System.out.println("[Login] Username: " + authViewModel.getUsername());
        System.out.println("  - Password: " + "*".repeat(authViewModel.getPassword().length()));
        System.out.println("[Login] Sending LoginCommand.");

        new LoginCommand(
                auctionService,
                new LoginFormValidator(),
                authPresenter,
                authViewModel.getUsername(),
                authViewModel.getPassword()
        ).execute();
    }

    @FXML
    private void handleSubmitRegister() {
        if (authViewModel == null) {
            authViewModel = new AuthViewModel();
        }

        String customerId = txtRegCustomerId != null ? txtRegCustomerId.getText() : "";
        String username = txtRegUsername != null ? txtRegUsername.getText() : "";
        String email = txtRegEmail != null ? txtRegEmail.getText() : "";
        String fullName = txtRegFullName != null ? txtRegFullName.getText() : "";
        String password = txtRegPassword != null ? txtRegPassword.getText() : "";
        String confirmPassword = txtRegConfirm != null ? txtRegConfirm.getText() : "";
        String role = cbRegRole != null ? cbRegRole.getValue() : "BIDDER";
        String organization = txtRegOrganization != null ? txtRegOrganization.getText() : "";

        authViewModel.setRegisterUsername(username);
        authViewModel.setRegisterPassword(password);
        authViewModel.setRegisterConfirmPassword(confirmPassword);
        authViewModel.setRegisterRole(role);
        authViewModel.setRegisterOrganization(organization);

        new RegisterCommand(
                auctionService,
                new RegisterFormValidator(),
                authPresenter,
                new RegisterForm(
                        customerId,
                        username,
                        email,
                        fullName,
                        password,
                        confirmPassword,
                        role,
                        organization
                )
        ).execute();
    }

    @FXML
    private void handleSubmitForgotPassword() {
        String username = txtForgotUsername != null ? txtForgotUsername.getText() : "";

        if (username == null || username.trim().isEmpty()) {
            if (lblForgotStatus != null) {
                lblForgotStatus.setText("❌ Please enter username or customer ID!");
                lblForgotStatus.setStyle("-fx-text-fill: red;");
            }
            return;
        }

        if (lblForgotStatus != null) {
            lblForgotStatus.setText("⏳ Processing...");
            lblForgotStatus.setStyle("-fx-text-fill: orange;");
        }

        System.out.println("[ForgotPassword] Processing for: " + username);
        auctionService.forgotPassword(username.trim());
    }

    @FXML
    public void handleLogout() {
        openLoginScreen();
    }

    @FXML
    public void openLoginScreen() {
        resetSessionState();
        sceneNavigator.showLogin();
    }

    @FXML
    private void handleBid() {
        ensureAuctionRoomActionsReady();
        if (bidActionHandler == null) {
            bidActionHandler = new BidActionHandler(auctionService, sessionStore, auctionRoomPresenter);
        }

        String roomId = sessionStore != null ? sessionStore.getCurrentRoomId() : "";
        double bidAmount = 0.0;

        try {
            if (txtBidAmount != null && !txtBidAmount.getText().isBlank()) {
                bidAmount = Double.parseDouble(txtBidAmount.getText());
            }
        } catch (NumberFormatException e) {
            System.err.println("[Bid Error] Invalid bid amount format.");
            return;
        }

        // Đã đổi sang gọi hàm placeBid(roomId, amount)
        bidActionHandler.placeBid(roomId, bidAmount);

        if (txtBidAmount != null) {
            txtBidAmount.clear();
        }
    }

    @FXML
    private void handleSendChat() {
        ensureAuctionRoomActionsReady();
        if (chatActionHandler == null) {
            chatActionHandler = new ChatActionHandler(auctionService);
        }

        String roomId = sessionStore != null ? sessionStore.getCurrentRoomId() : "";
        String message = txtChatInput != null ? txtChatInput.getText() : "";

        // Đã đổi sang gọi hàm sendChatMessage(roomId, message)
        chatActionHandler.sendChatMessage(roomId, message);

        if (txtChatInput != null) {
            txtChatInput.clear();
        }
    }

    @FXML
    public void handleBackToSelection() {
        roomTransitionHandler = new RoomTransitionHandler(auctionService, sessionStore, sceneNavigator, auctionTimerService, lobbyUserInfoBinder);
        roomTransitionHandler.backToLobby();
    }

    @FXML
    private void handleCloseAuction() {
        if (auctionCloseHandler == null) {
            auctionCloseHandler = new AuctionCloseHandler(auctionService);
        }

        String roomId = sessionStore != null ? sessionStore.getCurrentRoomId() : null;
        auctionCloseHandler.closeRoom(roomId);
    }

    private void ensureAuctionRoomActionsReady() {
        if (auctionService == null) {
            auctionService = new AuctionService(null);
        }
    }

    private void resetSessionState() {
        if (sessionStore != null) {
            sessionStore.clearSession(); // Đã đổi từ clear() thành clearSession()
        }
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (lblStatus != null) lblStatus.setText("");
    }

    // ============ SERVER CALLBACKS (Sửa lỗi cho ClientConnection) ============
    public void updateConnectionStatus(String status) {
        System.out.println("[ClientConnection] Trạng thái kết nối: " + status);
        // Có thể gán vào lblStatus nếu bạn muốn hiển thị trên giao diện
    }

    // Dùng kiểu Object để nhận Message, tránh lỗi import chưa có
    public void onServerResponse(Object message) {
        System.out.println("[ClientConnection] Nhận phản hồi từ server: " + message.toString());
    }
}