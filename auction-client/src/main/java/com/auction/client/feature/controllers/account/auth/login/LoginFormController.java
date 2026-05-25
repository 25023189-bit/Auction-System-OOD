package com.auction.client.feature.controllers.account.auth.login;

import com.auction.client.feature.auth.AuthActionFacade;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginFormController {
    private final TextField txtUsername;
    private final PasswordField txtPassword;
    private final AuthActionFacade authActionFacade;

    public LoginFormController(TextField txtUsername, PasswordField txtPassword, AuthActionFacade authActionFacade) {
        this.txtUsername = txtUsername;
        this.txtPassword = txtPassword;
        this.authActionFacade = authActionFacade;
    }

    public void submit() {
        authActionFacade.login(text(txtUsername), text(txtPassword));
    }

    private String text(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText();
    }
}
