package com.auction.client.core.navigation;

import javafx.stage.Stage;

/**
 * Hợp đồng quản lý trạng thái Stage khi đổi màn hình.
 *
 * Vai trò:
 * - Tách logic lưu/áp dụng kích thước cửa sổ khỏi SceneNavigator.
 * - Chuẩn hóa cách đặt title và kích thước cho các scene client.
 *
 * Luồng chính:
 * 1. Navigator gọi capture() trước khi đổi Scene nếu cần lưu trạng thái cũ.
 * 2. Navigator gọi apply() hoặc applyFixed() sau khi Scene mới được gắn vào Stage.
 *
 * Business rules:
 * - Title phải được cập nhật theo màn hình hiện tại.
 * - applyFixed() dùng khi màn hình yêu cầu kích thước cụ thể thay vì maximize mặc định.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; implementation JavaFX phải chạy trên JavaFX thread.
 * - Dependency: Stage và implementation như DefaultWindowStateHandler.
 */
public interface WindowStateHandler {
    void capture(Stage stage);

    void apply(Stage stage, String title);

    void applyFixed(Stage stage, String title, double width, double height);
}
