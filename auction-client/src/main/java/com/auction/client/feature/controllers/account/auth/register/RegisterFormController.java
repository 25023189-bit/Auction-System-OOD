package com.auction.client.feature.controllers.account.auth.register;

import com.auction.client.feature.auth.AuthActionFacade;
import com.auction.client.feature.auth.RegisterForm;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterFormController {
    private final AuthActionFacade authActionFacade;

    public RegisterFormController(AuthActionFacade authActionFacade) {
        this.authActionFacade = authActionFacade;
    }

    public void submit(TextField customerId, TextField username, TextField fullName,
                       PasswordField password, PasswordField confirmPassword,
                       ComboBox<String> role, TextField organization) {
        authActionFacade.register(new RegisterForm(
                text(customerId),
                text(username),
                text(fullName),
                text(password),
                text(confirmPassword),
                role != null ? role.getValue() : "BIDDER",
                text(organization)
        ));
    }

    private String text(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText();
    }
}
