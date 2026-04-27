package com.auction.client.feature.controllers;

import com.auction.client.feature.auth.*;
import com.auction.client.core.AuthPresenter;
import com.auction.client.core.SceneNavigator;
import com.auction.client.core.SessionStore;
import com.auction.client.feature.action.*;
import com.auction.client.feature.presenter.*;
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
        // Setup services
        auctionService = new AuctionService(null);
        sceneNavigator = new SceneNavigator();
        sessionStore = new SessionStore();
        authPresenter = new AuthPresenter();
        auctionRoomPresenter = new AuctionRoomPresenter();
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

        bidActionHandler.handle(new BidRequest(txtBidAmount != null ? txtBidAmount.getText() : ""));

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

        chatActionHandler.handle(new ChatRequest(txtChatInput != null ? txtChatInput.getText() : ""));

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
            sessionStore.clear();
        }
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (lblStatus != null) lblStatus.setText("");
    }
}
