package com.auction.client.core.ui;

/**
 * Hợp đồng bind dữ liệu model hoặc viewmodel vào control UI.
 *
 * Vai trò:
 * - Tách thao tác cập nhật control JavaFX khỏi message handler/controller.
 * - Chuẩn hóa một điểm nhận dữ liệu để render state mới.
 *
 * Luồng chính:
 * 1. Handler hoặc controller nhận model mới từ server/session.
 * 2. Binder đọc model và cập nhật các presenter/control liên quan.
 *
 * Business rules:
 * - bind() phải chấp nhận dữ liệu null nếu implementation cần bỏ qua an toàn.
 * - Binder chỉ nên cập nhật UI, không tự phát sinh request nghiệp vụ mới.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; binder JavaFX phải chạy trên JavaFX thread.
 * - Dependency: generic model T và các implementation như LobbyUserInfoBinder, AuctionRoomStateBinder.
 */
public interface ViewStateBinder<T> {
    void bind(T data);
}
