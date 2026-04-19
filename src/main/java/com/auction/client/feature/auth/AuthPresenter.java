package com.auction.client.feature.auth;

import com.auction.client.core.ui.ViewPresenter;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class AuthPresenter implements ViewPresenter {
    private final Label lblStatus;
    private final Label lblRegStatus;
    private final Label lblForgotStatus;
    private final TextField txtUsername;
    private final PasswordField txtPassword;
    private final TextField txtForgotUsername;
    private final PasswordField txtForgotNewPassword;
    private final PasswordField txtForgotConfirm;

    public AuthPresenter(Label lblStatus,
                         Label lblRegStatus,
                         Label lblForgotStatus,
                         TextField txtUsername,
                         PasswordField txtPassword,
                         TextField txtForgotUsername,
                         PasswordField txtForgotNewPassword,
                         PasswordField txtForgotConfirm) {
        this.lblStatus = lblStatus;
        this.lblRegStatus = lblRegStatus;
        this.lblForgotStatus = lblForgotStatus;
        this.txtUsername = txtUsername;
        this.txtPassword = txtPassword;
        this.txtForgotUsername = txtForgotUsername;
        this.txtForgotNewPassword = txtForgotNewPassword;
        this.txtForgotConfirm = txtForgotConfirm;
    }

    public void showLoginError(String message) {
        if (lblStatus != null) {
            lblStatus.setText("❌ " + message);
            lblStatus.setTextFill(Color.RED);
        }
    }

    public void showLoginSuccess() {
        if (lblStatus != null) {
            lblStatus.setText("✅ Đăng nhập thành công!");
            lblStatus.setTextFill(Color.GREEN);
        }
    }

    public void showRegisterError(String message) {
        if (lblRegStatus != null) {
            lblRegStatus.setText("❌ " + message);
            lblRegStatus.setTextFill(Color.RED);
        }
    }

    public void showResetError(String message) {
        if (lblForgotStatus != null) {
            lblForgotStatus.setText("❌ " + message);
            lblForgotStatus.setTextFill(Color.RED);
        }
    }

    public void clearResetForm() {
        if (txtForgotUsername != null) txtForgotUsername.clear();
        if (txtForgotNewPassword != null) txtForgotNewPassword.clear();
        if (txtForgotConfirm != null) txtForgotConfirm.clear();
    }

    public void clearLoginPassword() {
        if (txtPassword != null) txtPassword.clear();
    }

    public void setLoginUsername(String username) {
        if (txtUsername != null) txtUsername.setText(username);
    }

    @Override
    public void clear() {
        if (lblStatus != null) lblStatus.setText("");
        if (lblRegStatus != null) lblRegStatus.setText("");
        if (lblForgotStatus != null) lblForgotStatus.setText("");
    }
}