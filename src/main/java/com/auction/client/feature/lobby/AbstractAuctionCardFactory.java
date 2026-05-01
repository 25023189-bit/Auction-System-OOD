package com.auction.client.feature.lobby;

/**
 * Hợp đồng tạo card lobby với hai trạng thái hiển thị.
 *
 * Vai trò:
 * - Chuẩn hóa factory tạo card mặc định và card nổi bật.
 * - Tách renderer danh sách khỏi chi tiết dựng node UI.
 *
 * Luồng chính:
 * 1. LobbyRoomListRenderer nhận model phòng cần render.
 * 2. Renderer gọi createDefault() hoặc createHighlighted() để lấy node hiển thị.
 *
 * Business rules:
 * - Card mặc định dùng cho danh sách lobby thường.
 * - Card highlighted dùng khi cần nhấn mạnh một phòng mà vẫn giữ cùng dữ liệu nguồn.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; factory JavaFX không thread-safe.
 * - Dependency: generic T source, R node/result và implementation DefaultAuctionCardFactory.
 */
public interface AbstractAuctionCardFactory<T, R> {
    R createDefault(T source);
    R createHighlighted(T source);
}
