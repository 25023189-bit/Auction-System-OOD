package com.auction.client.feature.controllers.app.root;

import com.auction.client.feature.controllers.account.auth.AuthViewCoordinator;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.controllers.app.navigation.SessionLifecycleController;
import com.auction.client.feature.controllers.app.response.ClientResponseCoordinator;
import com.auction.client.feature.controllers.auction.lobby.LobbyViewCoordinator;
import com.auction.client.feature.controllers.auction.room.AuctionRoomViewCoordinator;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.network.socket.ServerMessageListener;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.support.FxThreadExecutor;
import com.auction.common.dto.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

import java.net.URL;
import java.util.ResourceBundle;

public class AuctionController implements Initializable, ServerMessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionController.class);

    @FXML VBox paneLogin, paneRegister, paneForgotPassword;
    @FXML Pane paneAuctionRoom;
    @FXML BorderPane paneMainLobby;

    @FXML TextField txtUsername, txtRegCustomerId, txtRegUsername, txtForgotUsername, txtRegOrganization, txtRegFullName;
    @FXML PasswordField txtPassword, txtRegPassword, txtRegConfirm, txtForgotNewPassword, txtForgotConfirm;
    @FXML Label lblStatus, lblRegStatus, lblForgotStatus;
    @FXML ComboBox<String> cbRegRole;
    @FXML Label lblRegOrganization;

    @FXML Label lblUsername, lblBalance, lblUsernameDisplay;
    @FXML Label lblCountdownTimer;
    @FXML Button btnCloseAuction, btnCreateAuction;
    @FXML FlowPane paneSelectAuction;

    @FXML Label lblAuctionItemName, lblCurrentPrice, lblParticipantCount, lblTimer;
    @FXML Label lblProductName, lblDescription, lblTimeLeft;
    @FXML TextArea txtChatLog;
    @FXML TextField txtBidAmount, txtChatInput;
    @FXML VBox vboxCurrencyRates, vboxNews;
    @FXML Button btnPlaceBid;
    @FXML TextArea txtItemDescriptionDisplay;
    @FXML ImageView imgProduct;

    @FXML Button btnToggleAutoBid;
    @FXML VBox paneAutoBid;
    @FXML TextField txtMaxBid;
    @FXML TextField txtAutoBidStep;

    @FXML AreaChart<String, Number> priceChart;

    private AuctionService auctionService;
    private SessionStore sessionStore;
    private FxThreadExecutor fxThreadExecutor;
    private SceneNavigator sceneNavigator;
    private AuctionControllerDependencies dependencies;

    private AuthViewCoordinator authViewCoordinator;
    private LobbyViewCoordinator lobbyViewCoordinator;
    private AuctionRoomViewCoordinator auctionRoomViewCoordinator;
    private SessionLifecycleController sessionLifecycleController;
    private ClientResponseCoordinator clientResponseCoordinator;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.debug("Initializing AuctionController.");
        if (dependencies == null) {
            dependencies = new AuctionControllerBootstrap().bootstrap(this, this);
        }
        auctionService = dependencies.auctionService();
        sessionStore = dependencies.sessionStore();
        fxThreadExecutor = dependencies.fxThreadExecutor();
        sceneNavigator = dependencies.sceneNavigator();

        if (hasAuthView()) {
            authViewCoordinator = new AuthViewCoordinator(
                    paneLogin, paneRegister, paneForgotPassword,
                    txtUsername, txtPassword,
                    txtRegCustomerId, txtRegUsername, txtRegFullName, txtRegPassword, txtRegConfirm,
                    cbRegRole, txtRegOrganization,
                    txtForgotUsername, txtForgotNewPassword, txtForgotConfirm,
                    lblStatus, lblRegStatus, lblForgotStatus, lblRegOrganization,
                    auctionService, sceneNavigator, sessionStore, dependencies.rolePolicy()
            );
            authViewCoordinator.initialize();
        }

        if (paneMainLobby != null) {
            lobbyViewCoordinator = new LobbyViewCoordinator(
                    lblUsername, lblBalance, btnCreateAuction, paneSelectAuction,
                    dependencies.rolePolicy(), auctionService, sessionStore
            );
        }

        sessionLifecycleController = new SessionLifecycleController(
                sessionStore, auctionService, sceneNavigator, AuctionControllerViewServices.createUiResetService(this)
        );

        if (paneAuctionRoom != null) {
            auctionRoomViewCoordinator = new AuctionRoomViewCoordinator(
                    lblAuctionItemName, lblProductName, lblCurrentPrice, lblTimer, lblTimeLeft,
                    lblParticipantCount, lblDescription, txtChatLog, txtItemDescriptionDisplay,
                    btnCloseAuction, btnPlaceBid, txtBidAmount, txtChatInput, lblUsername, imgProduct,
                    priceChart, btnToggleAutoBid, paneAutoBid, txtMaxBid, txtAutoBidStep,
                    auctionService, sessionStore, dependencies.rolePolicy(), sceneNavigator,
                    dependencies.clientConnection(), lobbyUserInfoBinder()
            );
            sessionLifecycleController.setAuctionTimer(auctionRoomViewCoordinator.auctionTimerService());
        }

        clientResponseCoordinator = null;
        ensureClientResponseCoordinatorReady();
        LOGGER.debug("AuctionController initialized.");
    }

    @FXML
    private void showRegisterScreen() {
        authViewCoordinator.showRegisterScreen();
    }

    @FXML
    private void showLoginScreen() {
        authViewCoordinator.showLoginScreen();
    }

    @FXML
    public void showForgotPasswordScreen() {
        authViewCoordinator.showForgotPasswordScreen();
    }

    @FXML
    private void handleLogin() {
        authViewCoordinator.handleLogin();
    }

    @FXML
    private void handleSubmitRegister() {
        authViewCoordinator.handleSubmitRegister();
    }

    @FXML
    private void handleSubmitForgotPassword() {
        authViewCoordinator.handleSubmitForgotPassword();
    }

    @FXML
    public void handleLogout() {
        openLoginScreen();
    }

    @FXML
    public void openLoginScreen() {
        sessionLifecycleController.openLoginScreen();
    }

    @FXML
    private void handleBid() {
        auctionRoomViewCoordinator.handleBid();
    }

    @FXML
    private void handleSendChat() {
        auctionRoomViewCoordinator.handleSendChat();
    }

    @FXML
    public void handleBackToSelection() {
        auctionRoomViewCoordinator.handleBackToSelection();
    }

    @FXML
    private void handleCloseAuction() {
        auctionRoomViewCoordinator.handleCloseAuction();
    }

    @FXML
    private void openSellerDashboard() {
        sceneNavigator.openSellerDashboard();
    }

    @FXML
    private void toggleAutoBidPanel() {
        auctionRoomViewCoordinator.toggleAutoBidPanel();
    }

    @FXML
    private void handleSetAutoBid() {
        auctionRoomViewCoordinator.handleSetAutoBid();
    }

    @FXML
    private void handleCancelAutoBid() {
        auctionRoomViewCoordinator.handleCancelAutoBid();
    }

    public void onServerResponse(Message msg) {
        ensureClientResponseCoordinatorReady();
        clientResponseCoordinator.handle(msg);
    }

    public void updateConnectionStatus(String status) {
        if (lblStatus == null) {
            return;
        }

        Runnable update = () -> {
            lblStatus.setText("Status: " + status);
        };

        if (Platform.isFxApplicationThread() || lblStatus.getScene() == null) {
            update.run();
        } else if (fxThreadExecutor != null) {
            fxThreadExecutor.execute(update);
        } else {
            Platform.runLater(update);
        }
    }

    private void ensureClientResponseCoordinatorReady() {
        if (clientResponseCoordinator == null) {
            clientResponseCoordinator = new ClientResponseCoordinator(
                    auctionService,
                    fxThreadExecutor,
                    authMessageHandler(),
                    lobbyMessageHandler(),
                    auctionRoomMessageHandler(),
                    sessionStore,
                    sceneNavigator,
                    lobbyUserInfoBinder(),
                    auctionRoomPresenter()
            );
        }
    }

    private boolean hasAuthView() {
        return paneLogin != null || paneRegister != null || paneForgotPassword != null;
    }

    private MessageHandler authMessageHandler() {
        return authViewCoordinator != null ? authViewCoordinator.authMessageHandler() : null;
    }

    private MessageHandler lobbyMessageHandler() {
        return lobbyViewCoordinator != null ? lobbyViewCoordinator.lobbyMessageHandler() : null;
    }

    private MessageHandler auctionRoomMessageHandler() {
        return auctionRoomViewCoordinator != null ? auctionRoomViewCoordinator.auctionRoomMessageHandler() : null;
    }

    private com.auction.client.feature.lobby.LobbyUserInfoBinder lobbyUserInfoBinder() {
        return lobbyViewCoordinator != null ? lobbyViewCoordinator.lobbyUserInfoBinder() : null;
    }

    private com.auction.client.feature.room.AuctionRoomPresenter auctionRoomPresenter() {
        return auctionRoomViewCoordinator != null ? auctionRoomViewCoordinator.auctionRoomPresenter() : null;
    }

    public void prepareForFxmlReload() {
        AuctionControllerFxmlReferences.clear(this);
        authViewCoordinator = null;
        lobbyViewCoordinator = null;
        auctionRoomViewCoordinator = null;
        sessionLifecycleController = null;
        clientResponseCoordinator = null;
    }
}

