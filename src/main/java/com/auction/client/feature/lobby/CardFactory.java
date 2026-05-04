package com.auction.client.feature.lobby;

/**
 * Hợp đồng factory tạo đối tượng hiển thị từ dữ liệu nguồn.
 *
 * Vai trò:
 * - Tách logic dựng node/card khỏi presenter hoặc renderer.
 * - Cho phép thay implementation tạo UI mà không đổi nơi render danh sách.
 *
 * Luồng chính:
 * 1. Presenter/renderer nhận source cần hiển thị.
 * 2. Caller gọi create(source) để nhận result tương ứng.
 *
 * Business rules:
 * - Result phải biểu diễn đúng source truyền vào.
 * - Factory không nên tự mutate danh sách container của caller.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: generic T source và R result.
 */
public interface CardFactory<T, R> {
    R create(T source);
}
