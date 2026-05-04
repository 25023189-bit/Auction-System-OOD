package com.auction.client.core.navigation;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Handler chuẩn hóa trạng thái Stage sau mỗi lần đổi Scene.
 *
 * Vai trò:
 * - Đặt title và trạng thái cửa sổ cho màn hình hiện tại.
 * - Maximize Stage theo vùng nhìn thấy của màn hình chính sau khi scene thay đổi.
 *
 * Luồng chính:
 * 1. SceneNavigator gọi capture() trước khi đổi scene và apply()/applyFixed() sau khi gắn scene mới.
 * 2. Handler đặt kích thước, tọa độ, maximized và lặp lại qua Platform.runLater để layout ổn định.
 *
 * Business rules:
 * - Client luôn ưu tiên màn hình làm việc lớn thay vì giữ kích thước nhỏ trước đó.
 * - Title của Stage phải phản ánh ngữ cảnh hiện tại như login, lobby hoặc auction room.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: thao tác Stage phải chạy trên JavaFX Application Thread.
 * - Dependency: WindowStateHandler, Stage, Screen, Rectangle2D, Platform.
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
