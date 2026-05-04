package com.auction.client.shared.support;

import javafx.stage.Stage;

/**
 * Hợp đồng tìm Stage chính của ứng dụng JavaFX.
 *
 * Vai trò:
 * - Cung cấp abstraction để các service/controller không tự duyệt Window.
 * - Trả về Stage hiện tại khi cần reset hoặc điều hướng cửa sổ.
 *
 * Luồng chính:
 * 1. Caller gọi resolveMainStage() tại thời điểm cần thao tác Stage.
 * 2. Implementation tìm Stage phù hợp từ môi trường JavaFX.
 *
 * Business rules:
 * - Không tìm thấy Stage thì trả null thay vì ném lỗi.
 * - Stage trả về phải là cửa sổ đang hiển thị.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; implementation JavaFX nên chạy trên JavaFX thread.
 * - Dependency: Stage và DefaultStageLocator.
 */
public interface StageLocator {
    Stage resolveMainStage();
}
