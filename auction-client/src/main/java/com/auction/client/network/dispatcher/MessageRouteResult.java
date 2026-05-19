package com.auction.client.network.dispatcher;

import com.auction.client.network.messaging.MessageHandler;
import com.auction.common.dto.Message;

/**
 * Kết quả định tuyến một phản hồi từ server ở phía client.
 *
 * Vai trò:
 * - Mang MessageHandler được AuctionMessageRouter chọn cùng Message gốc.
 * - Giúp router chỉ chọn đích đến, còn dispatcher thực thi handler.
 *
 * Luồng chính:
 * 1. AuctionMessageRouter tạo MessageRouteResult theo action của Message.
 * 2. AuctionMessageDispatcher nhận result và gọi handler.handle(message).
 *
 * Business rules:
 * - handler có thể null nếu không có handler phù hợp và không có fallback.
 * - message phải được giữ nguyên để handler cập nhật đúng UI/session.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: immutable sau khi khởi tạo.
 * - Dependency: MessageHandler, Message.
 */
public final class MessageRouteResult {
    private final MessageHandler handler;
    private final Message message;

    private MessageRouteResult(MessageHandler handler, Message message) {
        this.handler = handler;
        this.message = message;
    }

    public static MessageRouteResult matched(MessageHandler handler, Message message) {
        return new MessageRouteResult(handler, message);
    }

    public static MessageRouteResult unmatched(Message message) {
        return new MessageRouteResult(null, message);
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public MessageHandler getHandler() {
        return handler;
    }

    public Message getMessage() {
        return message;
    }
}
