package com.auction.client.shared.support;

import javafx.scene.control.Alert;

/**
 * Hợp đồng hiển thị thông báo để controller không phụ thuộc trực tiếp vào JavaFX Alert.
 */
public interface AlertService {
    void show(Alert.AlertType type, String title, String header, String content);

    void info(String title, String content);

    void warning(String title, String content);

    void error(String title, String content);
}
