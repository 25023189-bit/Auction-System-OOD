package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

/**
 * Hợp đồng xử lý một nhóm Message từ server dựa trên action.
 *
 * Vai trò:
 * - Chuẩn hóa cách kiểm tra handler có nhận action hay không.
 * - Cung cấp method handle() để xử lý message đã được router chọn.
 *
 * Luồng chính:
 * 1. ResponseRouter gọi supports(action) trên từng handler.
 * 2. Handler phù hợp nhận handle(message) để cập nhật UI/session hoặc gọi service.
 *
 * Business rules:
 * - supports() phải phản ánh đúng tập action mà handler xử lý.
 * - handle() chỉ nên được gọi sau khi supports(action) trả true, trừ fallback handler toàn cục.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: Message và ResponseRouter/AuctionMessageRouter.
 */
public interface MessageHandler {
    boolean supports(String action);
    void handle(Message message);
}
