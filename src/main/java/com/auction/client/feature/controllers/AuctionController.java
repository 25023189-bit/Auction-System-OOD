package com.auction.client.feature.controllers;

import com.auction.client.app.launcher.AdminDashboardLauncher;
import com.auction.client.app.launcher.DashboardLauncher;
import com.auction.client.app.launcher.SellerDashboardLauncher;
import com.auction.client.chatbot.ChatbotController;
import com.auction.client.core.navigation.DefaultWindowStateHandler;
import com.auction.client.core.navigation.FxSceneNavigator;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.core.navigation.WindowStateHandler;
import com.auction.client.feature.auth.AuthMessageHandler;
import com.auction.client.feature.auth.AuthPresenter;
import com.auction.client.feature.auth.LoginCommand;
import com.auction.client.feature.auth.LoginFormValidator;
import com.auction.client.feature.auth.RegisterCommand;
import com.auction.client.feature.auth.RegisterForm;
import com.auction.client.feature.auth.RegisterFormValidator;
import com.auction.client.feature.auth.ResetPasswordCommand;
import com.auction.client.feature.auth.ResetPasswordForm;
import com.auction.client.feature.auth.ResetPasswordFormValidator;
import com.auction.client.feature.lobby.AdvancedLobbyMessageHandler;
import com.auction.client.feature.lobby.AuctionCardFactory;
import com.auction.client.feature.lobby.DefaultAuctionCardFactory;
import com.auction.client.feature.lobby.LobbyPresenter;
import com.auction.client.feature.lobby.LobbyRoomListRenderer;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.feature.room.AuctionCloseHandler;
import com.auction.client.feature.room.AuctionRoomMessageHandler;
import com.auction.client.feature.room.AuctionRoomPresenter;
import com.auction.client.feature.room.AuctionRoomStateBinder;
import com.auction.client.feature.room.AuctionTimer;
import com.auction.client.feature.room.BidActionHandler;
import com.auction.client.feature.room.BidRequest;
import com.auction.client.feature.room.ChatActionHandler;
import com.auction.client.feature.room.ChatRequest;
import com.auction.client.feature.room.DefaultAuctionTimerService;
import com.auction.client.feature.room.RoomTransitionHandler;
import com.auction.client.feature.viewmodel.AuctionRoomViewModel;
import com.auction.client.feature.viewmodel.AuthViewModel;
import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.feature.viewmodel.LobbyViewModel;
import com.auction.client.network.messaging.AccountStatusFallbackHandler;
import com.auction.client.network.messaging.AdminFallbackHandler;
import com.auction.client.network.messaging.AuctionFlowFallbackHandler;
import com.auction.client.network.messaging.AuctionMessageRouter;
import com.auction.client.network.messaging.BalanceFallbackHandler;
import com.auction.client.network.messaging.FallbackMessageHandler;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.network.messaging.ResponseRouter;
import com.auction.client.session.InMemorySessionStore;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.client.shared.mapper.RoomDisplayMapper;
import com.auction.client.shared.mapper.RoomListMapper;
import com.auction.client.shared.support.AlertService;
import com.auction.client.shared.support.DefaultFxThreadExecutor;
import com.auction.client.shared.support.DefaultStageLocator;
import com.auction.client.shared.support.FxAlertService;
import com.auction.client.shared.support.FxThreadExecutor;
import com.auction.client.shared.support.StageLocator;
import com.auction.client.shared.support.UiResetService;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.common.role.DefaultRolePolicy;
import com.auction.common.role.RolePolicy;
import com.auction.server.service.AuctionService;
import com.auction.server.service.ClientConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller trung tâm của client JavaFX.
 * Kết nối các màn hình login, lobby, phòng đấu giá với service, session và router xử lý tin nhắn.
 */
public class AuctionController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionController.class);

    // ==========================================================
    // FXML FIELDS
    // ==========================================================
    @FXML private VBox paneLogin;
    @FXML private VBox paneRegister;
    @FXML private VBox paneForgotPassword;
    @FXML private Pane paneAuctionRoom;
    @FXML private BorderPane paneMainLobby;

    @FXML private TextField txtUsername;
    @FXML private TextField txtRegCustomerId;
    @FXML private TextField txtRegUsername;
    @FXML private TextField txtForgotUsername;
    @FXML private TextField txtRegOrganization;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtRegPassword;
    @FXML private PasswordField txtRegConfirm;
    @FXML private PasswordField txtForgotNewPassword;
    @FXML private PasswordField txtForgotConfirm;
    @FXML private Label lblStatus;
    @FXML private Label lblRegStatus;
    @FXML private Label lblForgotStatus;
    @FXML private ComboBox<String> cbRegRole;
    @FXML private Label lblRegOrganization;

    @FXML private Label lblUsername;
    @FXML private Label lblBalance;
    @FXML private Label lblUsernameDisplay;
    @FXML private Label lblCountdownTimer;
    @FXML private Button btnCloseAuction;
    @FXML private Button btnCreateAuction;

    @FXML private FlowPane paneSelectAuction;

    @FXML private Label lblAuctionItemName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblParticipantCount;
    @FXML private Label lblTimer;
    @FXML private Label lblProductName;
    @FXML private Label lblDescription;
    @FXML private Label lblTimeLeft;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtBidAmount;
    @FXML private TextField txtChatInput;
    @FXML private VBox vboxCurrencyRates;
    @FXML private VBox vboxNews;
    @FXML private Button btnPlaceBid;
    @FXML private TextArea txtItemDescriptionDisplay;
    @FXML private ImageView imgProduct;
    @FXML private ChatbotController chatbotController;
    @FXML private TextField txtRegEmail;
    @FXML private TextField txtRegFullName;
    @FXML private VBox paneAuctionLobby;
    @FXML private Label lblRoomId;
    @FXML private Label lblItemName;
    @FXML private Label lblItemPrice;
    @FXML private Label lblCurrentBid;
    @FXML private Label lblTimeRemaining;
    @FXML private TextArea txtChatDisplay;
    @FXML private ListView<String> lstBidHistory;
    @FXML private ListView<String> lstUsers;
    @FXML private ListView<String> lstAvailableRooms;
    @FXML private Label lblUserInfo;

    // ==========================================================
    // CORE SERVICE
    // ==========================================================
    private ClientConnection clientConnection;
    private AuctionService auctionService;
    // Đảm bảo mỗi controller chỉ khởi tạo kết nối socket một lần.
    private boolean isNetworkConnected = false;

    // ==========================================================
    // CORE ABSTRACTIONS
    // ==========================================================
    private SessionStore sessionStore;
    private WindowStateHandler windowStateHandler;
    private RolePolicy rolePolicy;
    private SceneNavigator sceneNavigator;

    // ==========================================================
    // SUPPORT SERVICES
    // ==========================================================
    private AlertService alertService;
    private StageLocator stageLocator;
    private UiResetService uiResetService;
    private FxThreadExecutor fxThreadExecutor;

    // ==========================================================
    // VIEW MODELS
    // ==========================================================
    private AuthViewModel authViewModel;
    private LobbyViewModel lobbyViewModel;
    private AuctionRoomViewModel auctionRoomViewModel;

    // ==========================================================
    // PRESENTERS / BINDERS / RENDERERS
    // ==========================================================
    private AuthPresenter authPresenter;
    private LobbyPresenter lobbyPresenter;
    private LobbyUserInfoBinder lobbyUserInfoBinder;

    private AuctionRoomPresenter auctionRoomPresenter;
    private AuctionRoomStateBinder auctionRoomStateBinder;
    private AuctionTimer auctionTimerService;

    private LobbyRoomListRenderer lobbyRoomListRenderer;
    private DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> roomDisplayMapper;

    // ==========================================================
    // MESSAGE HANDLERS
    // ==========================================================
    private MessageHandler authMessageHandler;
    private MessageHandler lobbyMessageHandler;
    private MessageHandler auctionRoomMessageHandler;

    private MessageHandler auctionFlowFallbackHandler;
    private MessageHandler balanceFallbackHandler;
    private MessageHandler adminFallbackHandler;
    private MessageHandler accountStatusFallbackHandler;

    private ResponseRouter responseRouter;

    // ==========================================================
    // ACTION HANDLERS / COMMAND SIDE
    // ==========================================================
    private BidActionHandler bidActionHandler;
    private ChatActionHandler chatActionHandler;
    private RoomTransitionHandler roomTransitionHandler;
    private AuctionCloseHandler auctionCloseHandler;

    // ==========================================================
    // DASHBOARD LAUNCHERS
    // ==========================================================
    private DashboardLauncher sellerDashboardLauncher;
    private DashboardLauncher adminDashboardLauncher;

    // ==========================================================
    // LOGIN / REGISTER / FORGOT SCREEN SWITCH
    // ==========================================================
    @FXML
    private void showRegisterScreen() {
        switchScreen(paneRegister);
        if (lblRegStatus != null) {
            lblRegStatus.setText("");
        }
    }

    @FXML
    private void showLoginScreen() {
        switchScreen(paneLogin);
        if (lblStatus != null) {
            lblStatus.setText("");
        }
    }

    @FXML
    public void showForgotPasswordScreen() {
        switchScreen(paneForgotPassword);
        if (lblForgotStatus != null) {
            lblForgotStatus.setText("");
        }
    }

    // ==========================================================
    // INITIALIZE
    // ==========================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.debug("Initializing AuctionController.");
        if (!isNetworkConnected) {
            clientConnection = new ClientConnection(this);
            auctionService = new AuctionService(clientConnection);
            clientConnection.connect();
            isNetworkConnected = true;
        }

        if (sessionStore == null) {
            sessionStore = new InMemorySessionStore();
        }
        if (windowStateHandler == null) {
            windowStateHandler = new DefaultWindowStateHandler();
        }
        if (rolePolicy == null) {
            rolePolicy = new DefaultRolePolicy();
        }
        if (sceneNavigator == null) {
            sceneNavigator = new FxSceneNavigator(this, windowStateHandler,
                    sessionStore, auctionService);
        }

        if (alertService == null) {
            alertService = new FxAlertService();
        }
        if (stageLocator == null) {
            stageLocator = new DefaultStageLocator();
        }
        if (fxThreadExecutor == null) {
            fxThreadExecutor = new DefaultFxThreadExecutor();
        }

        if (authViewModel == null) {
            authViewModel = new AuthViewModel();
        }
        if (lobbyViewModel == null) {
            lobbyViewModel = new LobbyViewModel();
        }
        if (auctionRoomViewModel == null) {
            auctionRoomViewModel = new AuctionRoomViewModel();
        }

        if (paneLogin != null || paneRegister != null || paneForgotPassword != null) {
            wireLoginView();
        }
        if (paneMainLobby != null) {
            wireLobbyView();
        }
        if (paneAuctionRoom != null) {
            wireAuctionRoomView();
        }

        if (cbRegRole != null && cbRegRole.getItems().isEmpty()) {
            cbRegRole.getItems().addAll("BIDDER", "SELLER");
            cbRegRole.setValue("BIDDER");
            cbRegRole.valueProperty().addListener((obs, oldValue, newValue) ->
                    updateRegisterOrganizationVisibility());
        }
        updateRegisterOrganizationVisibility();

        if (sellerDashboardLauncher == null) {
            sellerDashboardLauncher = new SellerDashboardLauncher(auctionService);
        }

        if (adminDashboardLauncher == null) {
            adminDashboardLauncher = new AdminDashboardLauncher(auctionService, sessionStore);
        }

        if (auctionRoomPresenter == null) {
            Label itemNameLabel = lblAuctionItemName != null ? lblAuctionItemName : lblProductName;
            Label timerLabel = lblTimer != null ? lblTimer : lblTimeLeft;
            auctionRoomPresenter = new AuctionRoomPresenter(
                    itemNameLabel, lblCurrentPrice, timerLabel, lblParticipantCount,
                    lblDescription, txtChatLog, txtItemDescriptionDisplay,
                    btnCloseAuction, btnPlaceBid, txtBidAmount);
        }

        if (auctionTimerService == null) {
            auctionTimerService = new DefaultAuctionTimerService(auctionRoomPresenter);
        }

        if (auctionRoomStateBinder == null) {
            auctionRoomStateBinder = new AuctionRoomStateBinder(
                    auctionRoomPresenter, sessionStore, rolePolicy);
        }

        if (auctionRoomMessageHandler == null) {
            auctionRoomMessageHandler = new AuctionRoomMessageHandler(
                    sessionStore, sceneNavigator, auctionRoomStateBinder,
                    auctionRoomPresenter, auctionTimerService);
        }

        if (roomTransitionHandler == null) {
            roomTransitionHandler = new RoomTransitionHandler(
                    auctionService, sessionStore, sceneNavigator,
                    auctionTimerService, lobbyUserInfoBinder);
        }
        LOGGER.debug("AuctionController initialized.");
    }

    private void wireLoginView() {
        authPresenter = new AuthPresenter(lblStatus, lblRegStatus, lblForgotStatus,
                txtUsername, txtPassword, txtForgotUsername,
                txtForgotNewPassword, txtForgotConfirm);
        authMessageHandler = new AuthMessageHandler(authPresenter, sceneNavigator,
                sessionStore, rolePolicy, auctionService);
        rebuildRouter();
    }

    private void wireLobbyView() {
        lobbyPresenter = new LobbyPresenter(paneSelectAuction, new AuctionCardFactory(auctionService));
        lobbyUserInfoBinder = new LobbyUserInfoBinder(lblUsername, lblBalance,
                btnCreateAuction, rolePolicy);
        roomDisplayMapper = new RoomDisplayMapper();
        lobbyRoomListRenderer = new LobbyRoomListRenderer(paneSelectAuction,
                new DefaultAuctionCardFactory(auctionService));
        lobbyMessageHandler = new AdvancedLobbyMessageHandler(new RoomListMapper(),
                roomDisplayMapper, lobbyRoomListRenderer);

        if (btnCreateAuction != null) {
            btnCreateAuction.setVisible(false);
            btnCreateAuction.setManaged(false);
        }

        if (sessionStore != null && sessionStore.getCurrentUser() != null) {
            lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
        }

        roomTransitionHandler = new RoomTransitionHandler(auctionService, sessionStore,
                sceneNavigator, auctionTimerService, lobbyUserInfoBinder);
        rebuildRouter();
    }

    private void wireAuctionRoomView() {
        Label itemNameLabel = lblAuctionItemName != null ? lblAuctionItemName : lblProductName;
        Label timerLabel = lblTimer != null ? lblTimer : lblTimeLeft;
        auctionRoomPresenter = new AuctionRoomPresenter(itemNameLabel, lblCurrentPrice,
                timerLabel, lblParticipantCount, lblDescription, txtChatLog,
                txtItemDescriptionDisplay, btnCloseAuction, btnPlaceBid, txtBidAmount);
        auctionTimerService = new DefaultAuctionTimerService(auctionRoomPresenter);
        auctionRoomStateBinder = new AuctionRoomStateBinder(auctionRoomPresenter,
                sessionStore, rolePolicy);
        auctionRoomMessageHandler = new AuctionRoomMessageHandler(sessionStore,
                sceneNavigator, auctionRoomStateBinder,
                auctionRoomPresenter, auctionTimerService);
        bidActionHandler = new BidActionHandler(auctionService, sessionStore, auctionRoomPresenter);
        chatActionHandler = new ChatActionHandler(auctionService);
        auctionCloseHandler = new AuctionCloseHandler(auctionService);
        roomTransitionHandler = new RoomTransitionHandler(auctionService, sessionStore,
                sceneNavigator, auctionTimerService, lobbyUserInfoBinder);

        if (btnCloseAuction != null) {
            btnCloseAuction.setVisible(false);
            btnCloseAuction.setManaged(false);
        }

        updateAuctionRoomUserLabel();

        if (sessionStore != null && sessionStore.getCurrentRoom() != null) {
            AuctionRoom room = sessionStore.getCurrentRoom();
            auctionRoomStateBinder.bind(room);
            auctionTimerService.start(room.getStartTime(), room.getEndTime());
            updateCloseAuctionButtonVisibility();
        }

        rebuildRouter();
    }

    private void rebuildRouter() {
        auctionFlowFallbackHandler = new AuctionFlowFallbackHandler(auctionService,
                sessionStore, sceneNavigator, lobbyUserInfoBinder, auctionRoomPresenter);
        balanceFallbackHandler = new BalanceFallbackHandler(auctionService,
                sessionStore, lobbyUserInfoBinder);
        adminFallbackHandler = new AdminFallbackHandler(sessionStore);
        accountStatusFallbackHandler = new AccountStatusFallbackHandler(sceneNavigator,
                sessionStore, auctionService);

        List<MessageHandler> primaryHandlers = new java.util.ArrayList<>();
        if (authMessageHandler != null) {
            primaryHandlers.add(authMessageHandler);
        }
        if (lobbyMessageHandler != null) {
            primaryHandlers.add(lobbyMessageHandler);
        }
        if (auctionRoomMessageHandler != null) {
            primaryHandlers.add(auctionRoomMessageHandler);
        }

        List<MessageHandler> fallbackHandlers = new java.util.ArrayList<>();
        if (auctionFlowFallbackHandler != null) {
            fallbackHandlers.add(auctionFlowFallbackHandler);
        }
        if (balanceFallbackHandler != null) {
            fallbackHandlers.add(balanceFallbackHandler);
        }
        if (adminFallbackHandler != null) {
            fallbackHandlers.add(adminFallbackHandler);
        }
        if (accountStatusFallbackHandler != null) {
            fallbackHandlers.add(accountStatusFallbackHandler);
        }

        responseRouter = new AuctionMessageRouter(primaryHandlers,
                new FallbackMessageHandler(fallbackHandlers));
    }

    // ==========================================================
    // USER ACTIONS
    // ==========================================================
    @FXML
    private void handleLogin() {
        LOGGER.debug("Login button clicked.");
        if (authViewModel == null) {
            authViewModel = new AuthViewModel();
        }

        authViewModel.setUsername(txtUsername != null ? txtUsername.getText() : "");
        authViewModel.setPassword(txtPassword != null ? txtPassword.getText() : "");

        LOGGER.debug("Read login form data from FXML. username={}, password={}",
                authViewModel.getUsername(),
                "*".repeat(authViewModel.getPassword().length()));
        LOGGER.debug("Sending LoginCommand.");

        new LoginCommand(auctionService, new LoginFormValidator(), authPresenter,
                authViewModel.getUsername(), authViewModel.getPassword()).execute();
    }

    @FXML
    private void handleSubmitRegister() {
        if (authViewModel == null) {
            authViewModel = new AuthViewModel();
        }

        String customerId = txtRegCustomerId != null ? txtRegCustomerId.getText() : "";
        String email = txtRegEmail != null ? txtRegEmail.getText() : "";
        String fullName = txtRegFullName != null ? txtRegFullName.getText() : "";

        authViewModel.setRegisterUsername(txtRegUsername != null ? txtRegUsername.getText() : "");
        authViewModel.setRegisterPassword(txtRegPassword != null ? txtRegPassword.getText() : "");
        authViewModel.setRegisterConfirmPassword(txtRegConfirm != null ? txtRegConfirm.getText() : "");
        authViewModel.setRegisterRole(cbRegRole != null ? cbRegRole.getValue() : "BIDDER");
        authViewModel.setRegisterOrganization(txtRegOrganization != null ? txtRegOrganization.getText() : "");

        new RegisterCommand(auctionService, new RegisterFormValidator(), authPresenter,
                new RegisterForm(customerId, authViewModel.getRegisterUsername(),
                        email, fullName, authViewModel.getRegisterPassword(),
                        authViewModel.getRegisterConfirmPassword(),
                        authViewModel.getRegisterRole(),
                        authViewModel.getRegisterOrganization())).execute();
    }

    @FXML
    private void handleSubmitForgotPassword() {
        if (authViewModel == null) {
            authViewModel = new AuthViewModel();
        }

        authViewModel.setForgotUsername(txtForgotUsername != null ? txtForgotUsername.getText() : "");
        authViewModel.setForgotPassword(txtForgotNewPassword != null ? txtForgotNewPassword.getText() : "");
        authViewModel.setForgotConfirmPassword(txtForgotConfirm != null ? txtForgotConfirm.getText() : "");

        new ResetPasswordCommand(auctionService, new ResetPasswordFormValidator(),
                authPresenter, new ResetPasswordForm(authViewModel.getForgotUsername(),
                authViewModel.getForgotPassword(), authViewModel.getForgotConfirmPassword())).execute();
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
        roomTransitionHandler = new RoomTransitionHandler(auctionService, sessionStore,
                sceneNavigator, auctionTimerService, lobbyUserInfoBinder);
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

    @FXML
    private void openSellerDashboard() {
        if (sellerDashboardLauncher == null) {
            sellerDashboardLauncher = new SellerDashboardLauncher(auctionService);
        }
        sellerDashboardLauncher.launch();
    }

    // ==========================================================
    // SERVER RESPONSE ENTRY
    // ==========================================================
    public void onServerResponse(Message msg) {
        if ("PRODUCT_DETAILS_SUCCESS".equals(msg.getAction())) {
            javafx.application.Platform.runLater(() ->
                    auctionService.fireProductDetailsReceived(msg.getData()));
            return;
        }

        fxThreadExecutor.execute(() -> responseRouter.route(msg));
    }

    // ==========================================================
    // OPTIONAL LEGACY BRIDGE
    // ==========================================================
    public void updateConnectionStatus(String status) {
        if (lblStatus != null) {
            lblStatus.setText("Status: " + status);
        }
    }

    // ==========================================================
    // INTERNAL HELPERS
    // ==========================================================
    private void switchScreen(Pane screenToShow) {
        if (paneLogin != null) {
            paneLogin.setVisible(false);
        }
        if (paneRegister != null) {
            paneRegister.setVisible(false);
        }
        if (paneAuctionRoom != null) {
            paneAuctionRoom.setVisible(false);
        }
        if (paneForgotPassword != null) {
            paneForgotPassword.setVisible(false);
        }
        if (paneMainLobby != null) {
            paneMainLobby.setVisible(false);
        }

        if (screenToShow != null) {
            screenToShow.setVisible(true);
            screenToShow.toFront();
        }
    }

    private void resetSessionState() {
        sessionStore.clearSession();

        ClientConnection.currentUser = null;
        if (auctionService != null) {
            auctionService.setCurrentUser(null);
        }

        if (auctionTimerService != null) {
            auctionTimerService.stop();
        }

        if (uiResetService != null) {
            uiResetService.resetSessionUi();
        }
    }

    private void ensureAuctionRoomActionsReady() {
        if (auctionRoomPresenter == null) {
            Label itemNameLabel = lblAuctionItemName != null ? lblAuctionItemName : lblProductName;
            Label timerLabel = lblTimer != null ? lblTimer : lblTimeLeft;
            auctionRoomPresenter = new AuctionRoomPresenter(itemNameLabel, lblCurrentPrice,
                    timerLabel, lblParticipantCount, lblDescription, txtChatLog,
                    txtItemDescriptionDisplay, btnCloseAuction, btnPlaceBid, txtBidAmount);
        }
        if (bidActionHandler == null) {
            bidActionHandler = new BidActionHandler(auctionService, sessionStore, auctionRoomPresenter);
        }
        if (chatActionHandler == null) {
            chatActionHandler = new ChatActionHandler(auctionService);
        }
    }

    private void updateCloseAuctionButtonVisibility() {
        if (btnCloseAuction == null || sessionStore == null) {
            return;
        }

        boolean visible = false;

        if (sessionStore.getCurrentUser() != null && sessionStore.getCurrentRoom() != null) {
            String currentUserId = sessionStore.getCurrentUser().getId();
            String role = sessionStore.getCurrentUser().getRole();
            String sellerIdOfRoom = sessionStore.getCurrentRoom().getSellerName();

            visible = "SELLER".equalsIgnoreCase(role)
                    && currentUserId != null
                    && currentUserId.equalsIgnoreCase(sellerIdOfRoom);
        }

        btnCloseAuction.setVisible(visible);
        btnCloseAuction.setManaged(visible);
    }

    private void updateAuctionRoomUserLabel() {
        if (lblUsername == null || sessionStore == null || sessionStore.getCurrentUser() == null) {
            return;
        }

        User user = sessionStore.getCurrentUser();
        String displayName = user.getUsername() != null && !user.getUsername().isBlank()
                ? user.getUsername()
                : user.getId();
        String prefix = "SELLER".equalsIgnoreCase(user.getRole()) ? "Seller: " : "User: ";
        lblUsername.setText(prefix + displayName);
    }

    private void updateRegisterOrganizationVisibility() {
        boolean sellerSelected = cbRegRole != null && "SELLER".equalsIgnoreCase(cbRegRole.getValue());

        if (txtRegOrganization != null) {
            txtRegOrganization.setVisible(sellerSelected);
            txtRegOrganization.setManaged(sellerSelected);
            if (!sellerSelected) {
                txtRegOrganization.clear();
            }
        }

        if (lblRegOrganization != null) {
            lblRegOrganization.setVisible(sellerSelected);
            lblRegOrganization.setManaged(sellerSelected);
        }
    }
}