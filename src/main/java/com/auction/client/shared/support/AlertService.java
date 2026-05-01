package com.auction.client.shared.support;

import javafx.scene.control.Alert;

/**
 * Hợp đồng hiển thị thông báo cho người dùng.
 *
 * Vai trò:
 * - Tách controller khỏi JavaFX Alert cụ thể.
 * - Cung cấp các shortcut info, warning và error dùng thống nhất trong UI.
 *
 * Luồng chính:
 * 1. Controller/handler gọi show() hoặc shortcut theo loại thông báo.
 * 2. Implementation hiển thị dialog bằng cơ chế UI cụ thể.
 *
 * Business rules:
 * - Nội dung lỗi/nghiệp vụ phải được truyền qua content để người dùng thấy rõ lý do.
 * - Header có thể null nếu màn hình không cần tiêu đề phụ.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; FxAlertService phải chạy trên JavaFX thread.
 * - Dependency: Alert.AlertType và implementation FxAlertService.
 */
public interface AlertService {
    void show(Alert.AlertType type, String title, String header, String content);
    void info(String title, String content);
    void warning(String title, String content);
    void error(String title, String content);
}
