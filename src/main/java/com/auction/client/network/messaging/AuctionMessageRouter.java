package com.auction.client.network.messaging;

import com.auction.common.dto.Message;

import java.util.List;

/**
 * Router phân phối Message server tới handler phù hợp.
 *
 * Vai trò:
 * - Duyệt danh sách handler chính theo thứ tự ưu tiên.
 * - Chuyển message không có handler chính sang fallback handler.
 *
 * Luồng chính:
 * 1. AuctionController.onServerResponse() gọi route(message).
 * 2. Router tìm handler đầu tiên supports(action), gọi handle(), hoặc chuyển sang fallback.
 *
 * Business rules:
 * - Thứ tự handler quyết định nơi xử lý khi nhiều handler cùng supports một action.
 * - Fallback handler được gọi khi không handler chính nào nhận message.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: danh sách handler không copy defensive, caller không nên mutate sau khi tạo.
 * - Dependency: ResponseRouter, MessageHandler, Message, List.
 */
public class AuctionMessageRouter implements ResponseRouter {
    private final List<MessageHandler> handlers;
    private final MessageHandler fallbackHandler;

    public AuctionMessageRouter(List<MessageHandler> handlers, MessageHandler fallbackHandler) {
        this.handlers = handlers;
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public void route(Message message) {
        // Ưu tiên handler theo màn hình hiện tại trước khi chuyển sang fallback.
        for (MessageHandler handler : handlers) {
            if (handler.supports(message.getAction())) {
                handler.handle(message);
                return;
            }
        }

        if (fallbackHandler != null) {
            fallbackHandler.handle(message);
        }
    }
}
