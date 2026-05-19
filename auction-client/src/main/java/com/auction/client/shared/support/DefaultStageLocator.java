package com.auction.client.shared.support;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * StageLocator tìm Stage chính đang hiển thị của ứng dụng.
 *
 * Vai trò:
 * - Dò danh sách Window hiện có của JavaFX.
 * - Trả về Stage đang show đầu tiên để các service khác thao tác.
 *
 * Luồng chính:
 * 1. Caller gọi resolveMainStage() khi cần Stage hiện tại.
 * 2. Locator duyệt Window.getWindows(), lọc window đang hiển thị và cast sang Stage.
 *
 * Business rules:
 * - Nếu không có cửa sổ đang show thì trả null.
 * - Chỉ Stage đang hiển thị mới được xem là target chính.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: đọc Window JavaFX nên gọi trên JavaFX Application Thread.
 * - Dependency: Stage, Window, StageLocator.
 */
public class DefaultStageLocator implements StageLocator {
    @Override
    public Stage resolveMainStage() {
        return (Stage) Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}
