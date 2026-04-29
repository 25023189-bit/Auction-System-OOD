package com.auction.client.core.navigation;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Chuẩn hóa trạng thái cửa sổ sau mỗi lần đổi Scene.
 * Mục tiêu là luôn mở full màn hình làm việc thay vì giữ kích thước nhỏ trước đó.
 */
public class DefaultWindowStateHandler implements WindowStateHandler {

    @Override
    public void capture(Stage stage) {
        // Scene transitions should not preserve smaller previous window sizes.
    }

    @Override
    public void apply(Stage stage, String title) {
        if (stage == null) return;

        // Tiêu đề được đổi theo màn hình hiện tại để người dùng biết đang ở vai trò nào.
        stage.setTitle(title);
        stage.setFullScreen(false);
        maximize(stage);
    }

    @Override
    public void applyFixed(Stage stage, String title, double width, double height) {
        apply(stage, title);
    }

    // Đặt kích thước theo vùng nhìn thấy của màn hình rồi bật maximized để JavaFX ổn định layout.
    private void maximize(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setMaximized(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(true);

        Platform.runLater(() -> {
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);
        });
    }
}
