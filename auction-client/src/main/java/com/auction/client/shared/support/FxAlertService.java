package com.auction.client.shared.support;

import javafx.scene.control.Alert;

/**
 * AlertService triển khai bằng JavaFX Alert.
 *
 * Vai trò:
 * - Tạo dialog thông báo cho các mức information, warning và error.
 * - Gom cấu hình title/header/content vào một điểm dùng chung.
 *
 * Luồng chính:
 * 1. Caller gọi show(), info(), warning() hoặc error().
 * 2. Service tạo Alert, gán nội dung và showAndWait().
 *
 * Business rules:
 * - Dialog được hiển thị dạng blocking để người dùng xác nhận trước khi tiếp tục.
 * - Shortcut info/warning/error phải map đúng Alert.AlertType.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: JavaFX Alert phải được tạo/hiển thị trên JavaFX Application Thread.
 * - Dependency: AlertService, javafx.scene.control.Alert.
 */
public class FxAlertService implements AlertService {
    @Override
    public void show(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void info(String title, String content) {
        show(Alert.AlertType.INFORMATION, title, null, content);
    }

    @Override
    public void warning(String title, String content) {
        show(Alert.AlertType.WARNING, title, null, content);
    }

    @Override
    public void error(String title, String content) {
        show(Alert.AlertType.ERROR, title, null, content);
    }
}
