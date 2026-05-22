package com.auction.client.feature.controllers.account.auth;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.auth.AuthActionFacade;
import com.auction.client.feature.auth.AuthMessageHandler;
import com.auction.client.feature.auth.AuthPresenter;
import com.auction.client.feature.controllers.account.auth.forgot.ForgotPasswordFormController;
import com.auction.client.feature.controllers.account.auth.login.LoginFormController;
import com.auction.client.feature.controllers.account.auth.register.RegisterFormController;
import com.auction.client.feature.controllers.account.auth.register.RegisterRoleVisibilityController;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.role.RolePolicy;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class AuthViewCoordinator {
    private final Pane paneLogin;
    private final Pane paneRegister;
    private final Pane paneForgotPassword;
    private final TextField txtUsername;
    private final PasswordField txtPassword;
    private final TextField txtRegCustomerId;
    private final TextField txtRegUsername;
    private final TextField txtRegFullName;
    private final PasswordField txtRegPassword;
    private final PasswordField txtRegConfirm;
    private final ComboBox<String> cbRegRole;
    private final TextField txtRegOrganization;
    private final TextField txtForgotUsername;
    private final PasswordField txtForgotNewPassword;
    private final PasswordField txtForgotConfirm;
    private final Label lblStatus;
    private final Label lblRegStatus;
    private final Label lblForgotStatus;
    private final Label lblRegOrganization;
    private final RegisterFormController registerFormController;
    private final ForgotPasswordFormController forgotPasswordFormController;
    private final RegisterRoleVisibilityController registerRoleVisibilityController;
    private final LoginFormController loginFormController;
    private final MessageHandler authMessageHandler;

    public AuthViewCoordinator(
            Pane paneLogin,
            Pane paneRegister,
            Pane paneForgotPassword,
            TextField txtUsername,
            PasswordField txtPassword,
            TextField txtRegCustomerId,
            TextField txtRegUsername,
            TextField txtRegFullName,
            PasswordField txtRegPassword,
            PasswordField txtRegConfirm,
            ComboBox<String> cbRegRole,
            TextField txtRegOrganization,
            TextField txtForgotUsername,
            PasswordField txtForgotNewPassword,
            PasswordField txtForgotConfirm,
            Label lblStatus,
            Label lblRegStatus,
            Label lblForgotStatus,
            Label lblRegOrganization,
            AuctionService auctionService,
            SceneNavigator sceneNavigator,
            SessionStore sessionStore,
            RolePolicy rolePolicy
    ) {
        this.paneLogin = paneLogin;
        this.paneRegister = paneRegister;
        this.paneForgotPassword = paneForgotPassword;
        this.txtUsername = txtUsername;
        this.txtPassword = txtPassword;
        this.txtRegCustomerId = txtRegCustomerId;
        this.txtRegUsername = txtRegUsername;
        this.txtRegFullName = txtRegFullName;
        this.txtRegPassword = txtRegPassword;
        this.txtRegConfirm = txtRegConfirm;
        this.cbRegRole = cbRegRole;
        this.txtRegOrganization = txtRegOrganization;
        this.txtForgotUsername = txtForgotUsername;
        this.txtForgotNewPassword = txtForgotNewPassword;
        this.txtForgotConfirm = txtForgotConfirm;
        this.lblStatus = lblStatus;
        this.lblRegStatus = lblRegStatus;
        this.lblForgotStatus = lblForgotStatus;
        this.lblRegOrganization = lblRegOrganization;

        AuthPresenter authPresenter = new AuthPresenter(
                lblStatus, lblRegStatus, lblForgotStatus,
                txtUsername, txtPassword, txtForgotUsername, txtForgotNewPassword, txtForgotConfirm
        );
        AuthActionFacade authActionFacade = new AuthActionFacade(auctionService, authPresenter);
        loginFormController = new LoginFormController(txtUsername, txtPassword, authActionFacade);
        registerFormController = new RegisterFormController(authActionFacade);
        forgotPasswordFormController = new ForgotPasswordFormController(authActionFacade);
        registerRoleVisibilityController = new RegisterRoleVisibilityController(cbRegRole, lblRegOrganization, txtRegOrganization);
        authMessageHandler = new AuthMessageHandler(authPresenter, sceneNavigator, sessionStore, rolePolicy, auctionService);
    }

    public void initialize() {
        if (cbRegRole != null && cbRegRole.getItems().isEmpty()) {
            cbRegRole.getItems().addAll("BIDDER", "SELLER");
            cbRegRole.setValue("BIDDER");
            cbRegRole.valueProperty().addListener((obs, oldValue, newValue) -> updateRegisterOrganizationVisibility());
        }
        updateRegisterOrganizationVisibility();
    }

    public void showRegisterScreen() {
        switchScreen(paneRegister);
        if (lblRegStatus != null) lblRegStatus.setText("");
    }

    public void showLoginScreen() {
        switchScreen(paneLogin);
        if (lblStatus != null) lblStatus.setText("");
    }

    public void showForgotPasswordScreen() {
        switchScreen(paneForgotPassword);
        if (lblForgotStatus != null) lblForgotStatus.setText("");
    }

    public void handleLogin() {
        loginFormController.submit();
    }

    public void handleSubmitRegister() {
        registerFormController.submit(txtRegCustomerId, txtRegUsername, txtRegFullName, txtRegPassword, txtRegConfirm, cbRegRole, txtRegOrganization);
    }

    public void handleSubmitForgotPassword() {
        forgotPasswordFormController.submit(txtForgotUsername, txtForgotNewPassword, txtForgotConfirm);
    }

    public MessageHandler authMessageHandler() {
        return authMessageHandler;
    }

    private void updateRegisterOrganizationVisibility() {
        registerRoleVisibilityController.update();
    }

    private void switchScreen(Pane screenToShow) {
        if (paneLogin != null) paneLogin.setVisible(false);
        if (paneRegister != null) paneRegister.setVisible(false);
        if (paneForgotPassword != null) paneForgotPassword.setVisible(false);
        if (screenToShow != null) {
            screenToShow.setVisible(true);
            screenToShow.toFront();
        }
    }
}
