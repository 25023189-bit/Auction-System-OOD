package com.auction.client.feature.presenter;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Presenter for authentication screens (Login, Register, Forgot Password)
 */
public class AuthPresenter {
    private Label lblStatus;
    private Label lblRegStatus;
    private Label lblForgotStatus;
    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtForgotUsername;
    private PasswordField txtForgotNewPassword;
    private PasswordField txtForgotConfirm;

    public AuthPresenter(Label lblStatus, Label lblRegStatus, Label lblForgotStatus,
                         TextField txtUsername, PasswordField txtPassword,
                         TextField txtForgotUsername, PasswordField txtForgotNewPassword,
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

    public void setLoginStatus(String status, boolean isError) {
        if (lblStatus != null) {
            lblStatus.setText(status);
            if (isError) {
                lblStatus.setStyle("-fx-text-fill: red;");
            } else {
                lblStatus.setStyle("-fx-text-fill: green;");
            }
        }
    }

    public void setRegisterStatus(String status, boolean isError) {
        if (lblRegStatus != null) {
            lblRegStatus.setText(status);
            if (isError) {
                lblRegStatus.setStyle("-fx-text-fill: red;");
            } else {
                lblRegStatus.setStyle("-fx-text-fill: green;");
            }
        }
    }

    public void setForgotPasswordStatus(String status, boolean isError) {
        if (lblForgotStatus != null) {
            lblForgotStatus.setText(status);
            if (isError) {
                lblForgotStatus.setStyle("-fx-text-fill: red;");
            } else {
                lblForgotStatus.setStyle("-fx-text-fill: orange;");
            }
        }
    }

    public void clearLoginForm() {
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (lblStatus != null) lblStatus.setText("");
    }

    public void clearForgotPasswordForm() {
        if (txtForgotUsername != null) txtForgotUsername.clear();
        if (txtForgotNewPassword != null) txtForgotNewPassword.clear();
        if (txtForgotConfirm != null) txtForgotConfirm.clear();
        if (lblForgotStatus != null) lblForgotStatus.setText("");
    }
}
