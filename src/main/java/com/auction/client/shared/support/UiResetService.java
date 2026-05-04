package com.auction.client.shared.support;

/**
 * Hợp đồng reset các control phụ thuộc session người dùng.
 *
 * Vai trò:
 * - Chuẩn hóa thao tác dọn UI khi logout hoặc session bị xóa.
 * - Tách logic reset control khỏi AuctionController.
 *
 * Luồng chính:
 * 1. Controller tạo implementation với các control hiện có.
 * 2. Khi session kết thúc, controller gọi resetSessionUi().
 *
 * Business rules:
 * - UI sau reset không được giữ thông tin user/room cũ.
 * - Các control theo role phải quay về trạng thái mặc định an toàn.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; implementation JavaFX không thread-safe.
 * - Dependency: implementation DefaultUiResetService.
 */
public interface UiResetService {
    void resetSessionUi();
}
