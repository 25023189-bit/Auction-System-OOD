package com.auction.client.feature.presenter;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Presenter xác thực legacy cập nhật trạng thái cho các form auth.
 *
 * Vai trò:
 * - Hiển thị trạng thái login, register và forgot password bằng label/style.
 * - Xóa form login hoặc forgot password khi cần.
 *
 * Luồng chính:
 * 1. Controller/handler legacy gọi setLoginStatus(), setRegisterStatus() hoặc setForgotPasswordStatus().
 * 2. Presenter cập nhật text/style của label và clear field theo lệnh.
 *
 * Business rules:
 * - Lỗi hiển thị màu đỏ, thành công hoặc trạng thái không lỗi dùng màu tương ứng.
 * - clear form phải xóa cả input mật khẩu để tránh giữ dữ liệu nhạy cảm.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: control JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: Label, TextField, PasswordField.
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
