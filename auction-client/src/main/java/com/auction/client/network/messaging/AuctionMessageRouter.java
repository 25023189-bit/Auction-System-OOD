package com.auction.client.network.messaging;

import com.auction.client.network.dispatcher.MessageRouteResult;
import com.auction.common.dto.Message;

import java.util.List;

/**
 * Router chọn MessageHandler phù hợp cho phản hồi từ server.
 *
 * Vai trò:
 * - Duyệt danh sách handler chính theo thứ tự ưu tiên.
 * - Chọn fallback handler khi không có handler chính nhận action.
 *
 * Luồng chính:
 * 1. AuctionController.onServerResponse() gọi route(message).
 * 2. Router tìm handler đầu tiên supports(action) và trả MessageRouteResult cho dispatcher.
 *
 * Business rules:
 * - Thứ tự handler quyết định nơi xử lý khi nhiều handler cùng supports một action.
 * - Fallback handler là đích đến khi không handler chính nào nhận message.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: danh sách handler được copy bất biến khi tạo router.
 * - Dependency: ResponseRouter, MessageHandler, MessageRouteResult, Message, List.
 */
public class AuctionMessageRouter implements ResponseRouter {
    private final List<MessageHandler> handlers;
    private final MessageHandler fallbackHandler;

    public AuctionMessageRouter(List<MessageHandler> handlers, MessageHandler fallbackHandler) {
        this.handlers = List.copyOf(handlers);
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public MessageRouteResult route(Message message) {
        // Ưu tiên handler theo màn hình hiện tại trước khi chọn fallback.
        for (MessageHandler handler : handlers) {
            if (handler.supports(message.getAction())) {
                return MessageRouteResult.matched(handler, message);
            }
        }

        if (fallbackHandler != null) {
            return MessageRouteResult.matched(fallbackHandler, message);
        }

        return MessageRouteResult.unmatched(message);
    }
}
