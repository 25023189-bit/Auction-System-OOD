package com.auction.server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry trung tâm lưu AuctionRuntimeState theo roomId.
 *
 * Vai trò:
 * - Tạo hoặc trả về runtime state của từng phòng đấu giá.
 * - Dọn state khi phiên bị đóng, hủy hoặc đã finalize.
 *
 * Luồng chính:
 * 1. AuctionRoomService gọi getState(roomId) khi xử lý join/bid/finalize.
 * 2. Handler/service gọi removeState(roomId) khi room không còn hoạt động.
 *
 * Business rules:
 * - Mỗi roomId chỉ có một AuctionRuntimeState dùng chung trong toàn server.
 * - State phải bị xóa khi room kết thúc để tránh dữ liệu participant/extension cũ ảnh hưởng phiên sau.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe ở cấp registry nhờ ConcurrentHashMap; state trả về vẫn cần caller synchronized khi mutate.
 * - Dependency: ConcurrentMap, ConcurrentHashMap, AuctionRuntimeState.
 */
public class AuctionStateManager {
    private static final ConcurrentMap<String, AuctionRuntimeState> STATES = new ConcurrentHashMap<>();

    private AuctionStateManager() {
    }

    public static AuctionRuntimeState getState(String roomId) {
        // Tạo state mới khi phòng lần đầu có user join/bid.
        return STATES.computeIfAbsent(roomId, id -> new AuctionRuntimeState());
    }

    public static void removeState(String roomId) {
        STATES.remove(roomId);
    }
}
