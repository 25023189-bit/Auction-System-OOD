package com.auction.client.feature.controllers;

import com.auction.client.app.launcher.AdminDashboardLauncher;
import com.auction.client.app.launcher.DashboardLauncher;
import com.auction.client.app.launcher.SellerDashboardLauncher;
import com.auction.client.AI.chatbot.ChatbotController;
import com.auction.client.core.navigation.DefaultWindowStateHandler;
import com.auction.client.core.navigation.FxSceneNavigator;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.core.navigation.WindowStateHandler;
import com.auction.client.feature.auth.*;
import com.auction.client.feature.lobby.*;
import com.auction.client.feature.room.*;
import com.auction.client.feature.controllers.viewmodel.AuthViewModel;
import com.auction.client.feature.controllers.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.network.dispatcher.AuctionMessageDispatcher;
import com.auction.client.network.dispatcher.MessageRouteResult;
import com.auction.client.network.dispatcher.ResponseDispatcher;
import com.auction.client.network.messaging.*;
import com.auction.client.network.socket.ClientConnection;
import com.auction.client.network.socket.ServerMessageListener;
import com.auction.client.service.AuctionService;
import com.auction.client.session.*;
import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.client.shared.mapper.RoomDisplayMapper;
import com.auction.client.shared.mapper.RoomListMapper;
import com.auction.client.shared.support.*;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.common.role.DefaultRolePolicy;
import com.auction.common.role.RolePolicy;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller trung tâm của client JavaFX.
 *
 * Vai trò:
 * - Kết nối các màn hình login, register, lobby và phòng đấu giá với service, session, presenter và handler.
 * - Nhận response từ server, route qua ResponseRouter rồi dispatch tới MessageHandler phù hợp.
 * - Hỗ trợ biểu đồ LineChart thời gian thực cho giá sản phẩm.
 */
public class AuctionController implements Initializable, ServerMessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionController.class);

    // ==========================================================
    // FXML FIELDS
    // ==========================================================
    @FXML private VBox paneLogin, paneRegister, paneForgotPassword;
    @FXML private Pane paneAuctionRoom;
    @FXML private BorderPane paneMainLobby;

    @FXML private TextField txtUsername, txtRegCustomerId, txtRegUsername, txtForgotUsername, txtRegOrganization, txtRegFullName;
    @FXML private PasswordField txtPassword, txtRegPassword, txtRegConfirm, txtForgotNewPassword, txtForgotConfirm;
    @FXML private Label lblStatus, lblRegStatus, lblForgotStatus;
    @FXML private ComboBox<String> cbRegRole;
    @FXML private Label lblRegOrganization;

    @FXML private Label lblUsername, lblBalance, lblUsernameDisplay;
    @FXML private Label lblCountdownTimer;
    @FXML private Button btnCloseAuction, btnCreateAuction;

    @FXML private FlowPane paneSelectAuction;

    @FXML private Label lblAuctionItemName, lblCurrentPrice, lblParticipantCount, lblTimer;
    @FXML private Label lblProductName, lblDescription, lblTimeLeft;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtBidAmount, txtChatInput;
    @FXML private VBox vboxCurrencyRates, vboxNews;
    @FXML private Button btnPlaceBid;
    @FXML private TextArea txtItemDescriptionDisplay;
    @FXML private ImageView imgProduct;
    @FXML private ChatbotController chatbotController;

    // ==========================================================
    // BIỂU ĐỒ GIÁ REAL-TIME (LINE CHART)
    // ==========================================================
    @FXML private LineChart<String, Number> priceChart;
    private XYChart.Series<String, Number> priceSeries;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ==========================================================
    // CORE SERVICE
    // ==========================================================
    private ClientConnection clientConnection;
    private AuctionService auctionService;
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
    private AuthActionFacade authActionFacade;

    // ==========================================================
    // VIEW MODELS
    // ==========================================================
    private AuthViewModel authViewModel;

    // ==========================================================
    // PRESENTERS / BINDERS / RENDERERS
    // ==========================================================
    private AuthPresenter authPresenter;
    private LobbyUserInfoBinder lobbyUserInfoBinder;

    private AuctionRoomPresenter auctionRoomPresenter;
    private AuctionRoomStateBinder auctionRoomStateBinder;
    private AuctionTimer auctionTimerService;

    private LobbyRoomListRenderer lobbyRoomListRenderer;
    private DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> roomDisplayMapper;

    // ==========================================================
    // MESSAGE HANDLERS & ROUTERS
    // ==========================================================
    private MessageHandler authMessageHandler;
    private MessageHandler lobbyMessageHandler;
    private MessageHandler auctionRoomMessageHandler;
    private MessageHandler auctionFlowFallbackHandler;
    private MessageHandler balanceFallbackHandler;
    private MessageHandler adminFallbackHandler;
    private MessageHandler accountStatusFallbackHandler;

    private ResponseRouter responseRouter;
    private ResponseDispatcher responseDispatcher;

    // ==========================================================
    // ACTION HANDLERS
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
        if (lblRegStatus != null) lblRegStatus.setText("");
    }

    @FXML
    private void showLoginScreen() {
        switchScreen(paneLogin);
        if (lblStatus != null) lblStatus.setText("");
    }

    @FXML
    public void showForgotPasswordScreen() {
        switchScreen(paneForgotPassword);
        if (lblForgotStatus != null) lblForgotStatus.setText("");
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

        if (sessionStore == null) sessionStore = new InMemorySessionStore();
        if (windowStateHandler == null) windowStateHandler = new DefaultWindowStateHandler();
        if (rolePolicy == null) rolePolicy = new DefaultRolePolicy();
        if (sceneNavigator == null) {
            sceneNavigator = new FxSceneNavigator(this, windowStateHandler, sessionStore, auctionService);
        }

        if (alertService == null) alertService = new FxAlertService();
        if (stageLocator == null) stageLocator = new DefaultStageLocator();
        if (fxThreadExecutor == null) fxThreadExecutor = new DefaultFxThreadExecutor();

        if (authViewModel == null) authViewModel = new AuthViewModel();

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
            cbRegRole.valueProperty().addListener((obs, oldValue, newValue) -> updateRegisterOrganizationVisibility());
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
                    btnCloseAuction, btnPlaceBid, txtBidAmount
            );
        }

        if (auctionTimerService == null) {
            auctionTimerService = new DefaultAuctionTimerService(auctionRoomPresenter);
        }

        if (auctionRoomStateBinder == null) {
            auctionRoomStateBinder = new AuctionRoomStateBinder(auctionRoomPresenter, sessionStore, rolePolicy);
        }

        if (auctionRoomMessageHandler == null) {
            auctionRoomMessageHandler = new AuctionRoomMessageHandler(
                    sessionStore, sceneNavigator, auctionRoomStateBinder, auctionRoomPresenter, auctionTimerService
            );
        }

        if (roomTransitionHandler == null) {
            roomTransitionHandler = new RoomTransitionHandler(
                    auctionService, sessionStore, sceneNavigator, auctionTimerService, lobbyUserInfoBinder
            );
        }
        LOGGER.debug("AuctionController initialized.");
    }

    private void wireLoginView() {
        authPresenter = new AuthPresenter(lblStatus, lblRegStatus, lblForgotStatus, txtUsername, txtPassword, txtForgotUsername, txtForgotNewPassword, txtForgotConfirm);
        authActionFacade = new AuthActionFacade(auctionService, authPresenter);
        authMessageHandler = new AuthMessageHandler(authPresenter, sceneNavigator, sessionStore, rolePolicy, auctionService);
        rebuildRouter();
    }

    private void wireLobbyView() {
        lobbyUserInfoBinder = new LobbyUserInfoBinder(lblUsername, lblBalance, btnCreateAuction, rolePolicy);
        roomDisplayMapper = new RoomDisplayMapper();
        lobbyRoomListRenderer = new LobbyRoomListRenderer(paneSelectAuction, new DefaultAuctionCardFactory(auctionService));
        lobbyMessageHandler = new AdvancedLobbyMessageHandler(new RoomListMapper(), roomDisplayMapper, lobbyRoomListRenderer);

        if (btnCreateAuction != null) {btnCreateAuction.setVisible(false);btnCreateAuction.setManaged(false);}

        if (sessionStore != null && sessionStore.getCurrentUser() != null) {
            lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
        }

        roomTransitionHandler = new RoomTransitionHandler(auctionService, sessionStore, sceneNavigator, auctionTimerService, lobbyUserInfoBinder);
        rebuildRouter();
    }

    private void wireAuctionRoomView() {
        Label itemNameLabel = lblAuctionItemName != null ? lblAuctionItemName : lblProductName;
        Label timerLabel = lblTimer != null ? lblTimer : lblTimeLeft;
        auctionRoomPresenter = new AuctionRoomPresenter(itemNameLabel, lblCurrentPrice, timerLabel, lblParticipantCount, lblDescription, txtChatLog, txtItemDescriptionDisplay, btnCloseAuction, btnPlaceBid, txtBidAmount);
        auctionTimerService = new DefaultAuctionTimerService(auctionRoomPresenter);
        auctionRoomStateBinder = new AuctionRoomStateBinder(auctionRoomPresenter, sessionStore, rolePolicy);
        auctionRoomMessageHandler = new AuctionRoomMessageHandler(sessionStore, sceneNavigator,auctionRoomStateBinder, auctionRoomPresenter, auctionTimerService);
        bidActionHandler = new BidActionHandler(auctionService, sessionStore, auctionRoomPresenter);
        chatActionHandler = new ChatActionHandler(auctionService);
        auctionCloseHandler = new AuctionCloseHandler(auctionService);
        roomTransitionHandler = new RoomTransitionHandler(auctionService, sessionStore, sceneNavigator, auctionTimerService, lobbyUserInfoBinder);

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

            // ==========================================
            // LOGIC KÍCH HOẠT BIỂU ĐỒ GIÁ REAL-TIME
            // ==========================================
            initChart(room.getStartingPrice());

            if (lblCurrentPrice != null) {
                lblCurrentPrice.textProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        String cleanPrice = newValue.replaceAll("[^\\d.]", "");
                        if (!cleanPrice.isEmpty()) {
                            double currentPrice = Double.parseDouble(cleanPrice);
                            updateChartOnNewBid(currentPrice);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Lỗi khi cập nhật biểu đồ: ", e);
                    }
                });
            }
            // ==========================================
        }

        rebuildRouter();
    }

    private void rebuildRouter() {
        auctionFlowFallbackHandler = new AuctionFlowFallbackHandler(auctionService, sessionStore, sceneNavigator, lobbyUserInfoBinder, auctionRoomPresenter);
        balanceFallbackHandler = new BalanceFallbackHandler(auctionService, sessionStore, lobbyUserInfoBinder);
        adminFallbackHandler = new AdminFallbackHandler(sessionStore);
        accountStatusFallbackHandler = new AccountStatusFallbackHandler(sceneNavigator, sessionStore, auctionService);

        java.util.List<MessageHandler> primaryHandlers = new java.util.ArrayList<>();
        if (authMessageHandler != null) primaryHandlers.add(authMessageHandler);
        if (lobbyMessageHandler != null) primaryHandlers.add(lobbyMessageHandler);
        if (auctionRoomMessageHandler != null) primaryHandlers.add(auctionRoomMessageHandler);

        java.util.List<MessageHandler> fallbackHandlers = new java.util.ArrayList<>();
        if (auctionFlowFallbackHandler != null) fallbackHandlers.add(auctionFlowFallbackHandler);
        if (balanceFallbackHandler != null) fallbackHandlers.add(balanceFallbackHandler);
        if (adminFallbackHandler != null) fallbackHandlers.add(adminFallbackHandler);
        if (accountStatusFallbackHandler != null) fallbackHandlers.add(accountStatusFallbackHandler);

        responseRouter = new AuctionMessageRouter(primaryHandlers, new FallbackMessageHandler(fallbackHandlers));
        if (responseDispatcher == null) {
            responseDispatcher = new AuctionMessageDispatcher();
        }
    }

    // ==========================================================
    // USER ACTIONS
    // ==========================================================
    @FXML
    private void handleLogin() {
        LOGGER.debug("Login button clicked.");
        if (authViewModel == null) authViewModel = new AuthViewModel();

        authViewModel.setUsername(txtUsername != null ? txtUsername.getText() : "");
        authViewModel.setPassword(txtPassword != null ? txtPassword.getText() : "");

        authActionFacade.login(authViewModel.getUsername(), authViewModel.getPassword());
    }

    @FXML
    private void handleSubmitRegister() {
        if (authViewModel == null) authViewModel = new AuthViewModel();

        String customerId = txtRegCustomerId != null ? txtRegCustomerId.getText() : "";
        String fullName = txtRegFullName != null ? txtRegFullName.getText() : "";

        authViewModel.setRegisterUsername(txtRegUsername != null ? txtRegUsername.getText() : "");
        authViewModel.setRegisterPassword(txtRegPassword != null ? txtRegPassword.getText() : "");
        authViewModel.setRegisterConfirmPassword(txtRegConfirm != null ? txtRegConfirm.getText() : "");
        authViewModel.setRegisterRole(cbRegRole != null ? cbRegRole.getValue() : "BIDDER");
        authViewModel.setRegisterOrganization(txtRegOrganization != null ? txtRegOrganization.getText() : "");

        authActionFacade.register(new RegisterForm(
                customerId, fullName, authViewModel.getRegisterUsername(),
                authViewModel.getRegisterPassword(), authViewModel.getRegisterConfirmPassword(),
                authViewModel.getRegisterRole(), authViewModel.getRegisterOrganization()
        ));
    }

    @FXML
    private void handleSubmitForgotPassword() {
        if (authViewModel == null) authViewModel = new AuthViewModel();

        authViewModel.setForgotUsername(txtForgotUsername != null ? txtForgotUsername.getText() : "");
        authViewModel.setForgotPassword(txtForgotNewPassword != null ? txtForgotNewPassword.getText() : "");
        authViewModel.setForgotConfirmPassword(txtForgotConfirm != null ? txtForgotConfirm.getText() : "");

        authActionFacade.resetPassword(new ResetPasswordForm(
                authViewModel.getForgotUsername(), authViewModel.getForgotPassword(), authViewModel.getForgotConfirmPassword()
        ));
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
        if (txtBidAmount != null) txtBidAmount.clear();
    }

    @FXML
    private void handleSendChat() {
        ensureAuctionRoomActionsReady();
        if (chatActionHandler == null) {
            chatActionHandler = new ChatActionHandler(auctionService);
        }
        chatActionHandler.handle(new ChatRequest(txtChatInput != null ? txtChatInput.getText() : ""));
        if (txtChatInput != null) txtChatInput.clear();
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
            javafx.application.Platform.runLater(() -> {
                auctionService.fireProductDetailsReceived(msg.getData());
            });
            return;
        }

        fxThreadExecutor.execute(() -> {
            MessageRouteResult routeResult = responseRouter.route(msg);
            responseDispatcher.dispatch(routeResult);
        });
    }

    public void updateConnectionStatus(String status) {
        if (lblStatus != null) {
            lblStatus.setText("Status: " + status);
        }
    }

    // ==========================================================
    // INTERNAL HELPERS
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

    private void resetSessionState() {
        sessionStore.clearSession();
        if (auctionService != null) auctionService.setCurrentUser(null);
        if (auctionTimerService != null) auctionTimerService.stop();
        if (uiResetService != null) uiResetService.resetSessionUi();
    }

    private void ensureAuctionRoomActionsReady() {
        if (auctionRoomPresenter == null) {
            Label itemNameLabel = lblAuctionItemName != null ? lblAuctionItemName : lblProductName;
            Label timerLabel = lblTimer != null ? lblTimer : lblTimeLeft;
            auctionRoomPresenter = new AuctionRoomPresenter(itemNameLabel, lblCurrentPrice, timerLabel, lblParticipantCount, lblDescription, txtChatLog, txtItemDescriptionDisplay, btnCloseAuction, btnPlaceBid, txtBidAmount);
        }
        if (bidActionHandler == null) bidActionHandler = new BidActionHandler(auctionService, sessionStore, auctionRoomPresenter);
        if (chatActionHandler == null) chatActionHandler = new ChatActionHandler(auctionService);
    }

    private void updateCloseAuctionButtonVisibility() {
        if (btnCloseAuction == null || sessionStore == null) return;
        boolean visible = false;
        if (sessionStore.getCurrentUser() != null && sessionStore.getCurrentRoom() != null) {
            String currentUserId = sessionStore.getCurrentUser().getId();
            String role = sessionStore.getCurrentUser().getRole();
            String sellerIdOfRoom = sessionStore.getCurrentRoom().getSellerName();
            visible = "SELLER".equalsIgnoreCase(role) && currentUserId != null && currentUserId.equalsIgnoreCase(sellerIdOfRoom);
        }
        btnCloseAuction.setVisible(visible);
        btnCloseAuction.setManaged(visible);
    }

    private void updateAuctionRoomUserLabel() {
        if (lblUsername == null || sessionStore == null || sessionStore.getCurrentUser() == null) return;
        User user = sessionStore.getCurrentUser();
        String displayName = user.getUsername() != null && !user.getUsername().isBlank() ? user.getUsername() : user.getId();
        String prefix = "SELLER".equalsIgnoreCase(user.getRole()) ? "Seller: " : "User: ";
        lblUsername.setText(prefix + displayName);
    }

    private void updateRegisterOrganizationVisibility() {
        boolean sellerSelected = cbRegRole != null && "SELLER".equalsIgnoreCase(cbRegRole.getValue());
        if (txtRegOrganization != null) {
            txtRegOrganization.setVisible(sellerSelected);
            txtRegOrganization.setManaged(sellerSelected);
            if (!sellerSelected) txtRegOrganization.clear();
        }
        if (lblRegOrganization != null) {
            lblRegOrganization.setVisible(sellerSelected);
            lblRegOrganization.setManaged(sellerSelected);
        }
    }

    // ==========================================================
    // CÁC HÀM XỬ LÝ BIỂU ĐỒ (LINE CHART)
    // ==========================================================
    public void initChart(double startPrice) {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Lịch sử giá");

        if (priceChart != null) {
            priceChart.getData().clear();
            priceChart.getData().add(priceSeries);
        }

        String timeNow = LocalTime.now().format(timeFormatter);
        priceSeries.getData().add(new XYChart.Data<>(timeNow, startPrice));
    }

    public void updateChartOnNewBid(double newPrice) {
        Platform.runLater(() -> {
            String timeNow = LocalTime.now().format(timeFormatter);
            priceSeries.getData().add(new XYChart.Data<>(timeNow, newPrice));

            if (priceSeries.getData().size() > 15) {
                priceSeries.getData().remove(0);
            }
        });
    }
}