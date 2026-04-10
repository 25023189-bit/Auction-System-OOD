package com.auction.client.support;

import javafx.scene.control.Alert;

public interface AlertService {
    void show(Alert.AlertType type, String title, String header, String content);
    void info(String title, String content);
    void warning(String title, String content);
    void error(String title, String content);
}