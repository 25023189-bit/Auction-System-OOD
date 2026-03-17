package client.controllers;

import client.network.ServerConnection;
import shared.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.List;

public class LoginController {
    @FXML private TextField txtMaKhachHang;
    @FXML private ComboBox<String> cbMaPhien;

    private ServerConnection conn = ServerConnection.getInstance();

    // Hàm này tự chạy khi vừa mở Form để kết nối Server và lấy danh sách phiên
    @FXML
    public void initialize() {
        conn.connect("localhost", 8888, this::processMessage);
        conn.send(new Message("GET_AUCTIONS", "Guest", null));
    }

    // Đã đổi tên hàm thành handleLoginClick để khớp với file FXML của sếp
    @FXML
    private void handleLoginClick() {
        String id = txtMaKhachHang.getText();
        String phienRaw = cbMaPhien.getValue();

        if (id.isEmpty() || phienRaw == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập mã KH và chọn phiên!").show();
            return;
        }

        String maPhien = phienRaw.split(" - ")[0];

        // Gửi lệnh đăng nhập xuống Server
        conn.send(new Message("LOGIN", "Client", new String[]{id, maPhien}));
    }

    private void processMessage(Message msg) {

    }
}