package com.auction.client.feature.auth;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.common.role.RolePolicy;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFeatureTest {
    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Started by another JavaFX test.
        }
    }

    @Test
    void validatorsReturnSpecificFailuresAndAcceptValidForms() {
        LoginFormValidator login = new LoginFormValidator();
        assertFalse(login.validate(new LoginForm(" ", "secret")).isValid());
        assertEquals("Password is required!", login.validate(new LoginForm("user", " ")).getMessage());
        assertTrue(login.validate(new LoginForm("user", "secret")).isValid());

        RegisterFormValidator register = new RegisterFormValidator();
        assertEquals("Username is required!",
                register.validate(new RegisterForm("", "", "", "p", "p", "BIDDER", "")).getMessage());
        assertEquals("Password is required!",
                register.validate(new RegisterForm("", "u", "", "", "", "BIDDER", "")).getMessage());
        assertEquals("Password confirmation does not match!",
                register.validate(new RegisterForm("", "u", "", "a", "b", "BIDDER", "")).getMessage());
        assertEquals("Seller organization is required!",
                register.validate(new RegisterForm("", "u", "", "a", "a", "SELLER", " ")).getMessage());
        assertTrue(register.validate(new RegisterForm("", "u", "", "a", "a", "SELLER", "Org")).isValid());

        ResetPasswordFormValidator reset = new ResetPasswordFormValidator();
        assertEquals("Please enter your username!",
                reset.validate(new ResetPasswordForm("", "a", "a")).getMessage());
        assertEquals("Please enter a new password!",
                reset.validate(new ResetPasswordForm("u", "", "")).getMessage());
        assertEquals("Password confirmation does not match!",
                reset.validate(new ResetPasswordForm("u", "a", "b")).getMessage());
        assertTrue(reset.validate(new ResetPasswordForm("u", "a", "a")).isValid());
    }

    @Test
    void facadeOnlySendsValidatedNormalizedRequests() {
        AuctionService service = mock(AuctionService.class);
        AuthPresenter presenter = mock(AuthPresenter.class);
        AuthActionFacade facade = new AuthActionFacade(service, presenter);

        facade.login(" ", "password");
        verify(presenter).showLoginError("Username is required!");
        verify(service, never()).login(" ", "password");

        facade.login(" user ", " pass ");
        verify(service).login("user", "pass");

        facade.register(new RegisterForm("ID", " seller ", null, "pw", "pw", "SELLER", "Org"));
        verify(service).register("ID", "seller", "", "pw", "SELLER", "Org");

        facade.register(new RegisterForm("ID2", " bidder ", " Name ", "pw", "pw", "BIDDER", "ignored"));
        verify(service).register("ID2", "bidder", "Name", "pw", "BIDDER", null);

        facade.resetPassword(new ResetPasswordForm(" ", "pw", "pw"));
        verify(presenter).showResetError("Please enter your username!");
        facade.resetPassword(new ResetPasswordForm(" user ", " new ", " new "));
        verify(service).resetPassword("user", "new", "new");
    }

    @Test
    void presenterUpdatesAndClearsControls() {
        Label loginStatus = new Label();
        Label registerStatus = new Label();
        Label resetStatus = new Label();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField resetUsername = new TextField("u");
        PasswordField newPassword = new PasswordField();
        PasswordField confirm = new PasswordField();
        password.setText("password");
        newPassword.setText("new");
        confirm.setText("new");

        AuthPresenter presenter = new AuthPresenter(
                loginStatus, registerStatus, resetStatus, username, password,
                resetUsername, newPassword, confirm);

        presenter.showLoginError("bad");
        assertEquals("Error: bad", loginStatus.getText());
        assertEquals(Color.RED, loginStatus.getTextFill());
        presenter.showLoginSuccess();
        assertEquals("Login successful!", loginStatus.getText());
        assertEquals(Color.GREEN, loginStatus.getTextFill());
        presenter.showRegisterError("taken");
        presenter.showResetError("weak");
        assertEquals("Error: taken", registerStatus.getText());
        assertEquals("Error: weak", resetStatus.getText());

        presenter.setLoginUsername("resolved");
        presenter.clearLoginPassword();
        presenter.clearResetForm();
        presenter.clear();
        assertEquals("resolved", username.getText());
        assertEquals("", password.getText());
        assertEquals("", resetUsername.getText());
        assertEquals("", newPassword.getText());
        assertEquals("", confirm.getText());
        assertEquals("", loginStatus.getText());
        assertEquals("", registerStatus.getText());
        assertEquals("", resetStatus.getText());
    }

    @Test
    void authMessageHandlerRoutesLoginRegistrationAndFailures() {
        AuthPresenter presenter = mock(AuthPresenter.class);
        SceneNavigator navigator = mock(SceneNavigator.class);
        SessionStore session = mock(SessionStore.class);
        RolePolicy rolePolicy = mock(RolePolicy.class);
        AuctionService service = mock(AuctionService.class);
        AuthMessageHandler handler = new AuthMessageHandler(presenter, navigator, session, rolePolicy, service);

        assertTrue(handler.supports("LOGIN_SUCCESS"));
        assertTrue(handler.supports("REGISTER_FAIL"));
        assertFalse(handler.supports("ROOM_LIST"));

        User bidder = new User();
        bidder.setCustomerId("U1");
        bidder.setUsername("bidder");
        when(rolePolicy.isAdmin(bidder)).thenReturn(false);
        handler.handle(new Message("LOGIN_SUCCESS", "SERVER", bidder));
        verify(session).setCurrentUser(bidder);
        verify(session).setCurrentUsername("bidder");
        verify(service).setCurrentUser("U1");
        verify(navigator).showLobby();
        verify(service).getRooms();

        User admin = new User();
        admin.setCustomerId("A1");
        when(rolePolicy.isAdmin(admin)).thenReturn(true);
        handler.handle(new Message("LOGIN_SUCCESS", "SERVER", admin));
        verify(navigator).openAdminDashboard();

        handler.handle(new Message("LOGIN_FAIL", "SERVER", "invalid"));
        handler.handle(new Message("REGISTER_FAIL", "SERVER", "exists"));
        handler.handle(new Message("RESET_FAIL", "SERVER", "weak"));
        handler.handle(new Message("REGISTER_SUCCESS", "SERVER", "ok"));
        verify(presenter).showLoginError("invalid");
        verify(presenter).showRegisterError("exists");
        verify(presenter).showResetError("weak");
        verify(presenter).clearLoginPassword();
        verify(navigator).showLogin();
    }
}
