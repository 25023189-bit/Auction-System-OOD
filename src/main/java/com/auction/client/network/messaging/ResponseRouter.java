package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

/**
 * Hợp đồng điều phối phản hồi server đến handler phù hợp.
 *
 * Vai trò:
 * - Tách nơi nhận Message từ socket khỏi chi tiết xử lý từng action.
 * - Cung cấp API route(message) cho controller client.
 *
 * Luồng chính:
 * 1. AuctionController nhận Message từ ClientConnection.
 * 2. Controller gọi route(message), implementation chọn handler tương ứng.
 *
 * Business rules:
 * - Mỗi message phải được route theo action.
 * - Implementation nên có fallback để log hoặc xử lý action chưa biết.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; router UI thường chạy trên JavaFX thread.
 * - Dependency: Message và implementation AuctionMessageRouter.
 */
public interface ResponseRouter {
    void route(Message message);
}
