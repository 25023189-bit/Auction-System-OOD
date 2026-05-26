package com.auction.client.feature.controllers.account.auth;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.role.RolePolicy;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthViewCoordinatorTest {
    private Pane login;
    private Pane register;
    private Pane forgot;
    private TextField username;
    private PasswordField password;
    private TextField customerId;
    private TextField regUsername;
    private TextField fullName;
    private PasswordField regPassword;
    private PasswordField regConfirm;
    private ComboBox<String> role;
    private TextField organization;
    private TextField forgotUsername;
    private PasswordField forgotPassword;
    private PasswordField forgotConfirm;
    private Label status;
    private Label regStatus;
    private Label forgotStatus;
    private Label orgLabel;
    private AuctionService service;
    private AuthViewCoordinator coordinator;

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Started by another JavaFX test.
        }
    }

    @BeforeEach
    void setUp() {
        login = new Pane();
        register = new Pane();
        forgot = new Pane();
        username = new TextField();
        password = new PasswordField();
        customerId = new TextField();
        regUsername = new TextField();
        fullName = new TextField();
        regPassword = new PasswordField();
        regConfirm = new PasswordField();
        role = new ComboBox<>();
        organization = new TextField();
        forgotUsername = new TextField();
        forgotPassword = new PasswordField();
        forgotConfirm = new PasswordField();
        status = new Label("old");
        regStatus = new Label("old");
        forgotStatus = new Label("old");
        orgLabel = new Label();
        service = mock(AuctionService.class);
        coordinator = new AuthViewCoordinator(
                login, register, forgot, username, password, customerId, regUsername, fullName,
                regPassword, regConfirm, role, organization, forgotUsername, forgotPassword,
                forgotConfirm, status, regStatus, forgotStatus, orgLabel, service,
                mock(SceneNavigator.class), mock(SessionStore.class), mock(RolePolicy.class));
    }

    @Test
    void initializesRolesAndSwitchesScreens() {
        coordinator.initialize();
        assertEquals(2, role.getItems().size());
        assertEquals("BIDDER", role.getValue());
        assertFalse(organization.isVisible());

        role.setValue("SELLER");
        assertTrue(organization.isVisible());
        assertTrue(orgLabel.isManaged());

        coordinator.showRegisterScreen();
        assertTrue(register.isVisible());
        assertFalse(login.isVisible());
        assertEquals("", regStatus.getText());
        coordinator.showForgotPasswordScreen();
        assertTrue(forgot.isVisible());
        assertEquals("", forgotStatus.getText());
        coordinator.showLoginScreen();
        assertTrue(login.isVisible());
        assertEquals("", status.getText());
        assertNotNull(coordinator.authMessageHandler());
    }

    @Test
    void delegatesValidFormSubmissionsToAuctionService() {
        coordinator.initialize();
        username.setText(" user ");
        password.setText(" password ");
        coordinator.handleLogin();
        verify(service).login("user", "password");

        customerId.setText("S1");
        regUsername.setText("seller");
        fullName.setText(" Seller Name ");
        regPassword.setText("pw");
        regConfirm.setText("pw");
        role.setValue("SELLER");
        organization.setText("Gallery");
        coordinator.handleSubmitRegister();
        verify(service).register("S1", "seller", "Seller Name", "pw", "SELLER", "Gallery");

        forgotUsername.setText(" user ");
        forgotPassword.setText(" new ");
        forgotConfirm.setText(" new ");
        coordinator.handleSubmitForgotPassword();
        verify(service).resetPassword("user", "new", "new");
    }
}
