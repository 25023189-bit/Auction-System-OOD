package com.auction.client.core.ui;

/**
 * Hợp đồng chung cho presenter điều khiển trạng thái hiển thị.
 *
 * Vai trò:
 * - Chuẩn hóa thao tác xóa nội dung hoặc trạng thái lỗi khỏi UI.
 * - Cho phép controller dùng presenter mà không phụ thuộc loại màn hình cụ thể.
 *
 * Luồng chính:
 * 1. Controller tạo presenter cho từng màn hình hoặc khu vực UI.
 * 2. Khi cần reset view, controller gọi clear() qua interface.
 *
 * Business rules:
 * - clear() chỉ nên dọn trạng thái hiển thị, không gửi request mạng.
 * - Presenter không nên giữ nghiệp vụ server-side.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; presenter JavaFX không thread-safe.
 * - Dependency: các implementation presenter trong feature auth/lobby/room.
 */
public interface ViewPresenter {
    void clear();
}
