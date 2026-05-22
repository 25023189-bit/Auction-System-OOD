package com.auction.client.feature.controllers.account.auth.forgot;

import com.auction.client.feature.auth.AuthActionFacade;
import com.auction.client.feature.auth.ResetPasswordForm;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordFormController {
    private final AuthActionFacade authActionFacade;

    public ForgotPasswordFormController(AuthActionFacade authActionFacade) {
        this.authActionFacade = authActionFacade;
    }

    public void submit(TextField username, PasswordField newPassword, PasswordField confirmPassword) {
        authActionFacade.resetPassword(new ResetPasswordForm(text(username), text(newPassword), text(confirmPassword)));
    }

    private String text(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText();
    }
}
