package com.auction.client.network.dispatcher;

import com.auction.common.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher thực thi MessageRouteResult ở phía client.
 *
 * Vai trò:
 * - Validate kết quả route trước khi gọi MessageHandler.
 * - Ghi log khi không có handler hoặc handler phát sinh lỗi.
 *
 * Luồng chính:
 * 1. AuctionController gọi dispatch(result) trên JavaFX Application Thread.
 * 2. Dispatcher gọi handler.handle(message) nếu result hợp lệ.
 *
 * Business rules:
 * - Không gọi handler khi routeResult/message null.
 * - Không tự sửa UI; mọi cập nhật UI vẫn nằm trong handler nghiệp vụ.
 *
 * Ghi chú kỹ thuật:
 * - Stateless, có thể dùng lại cho toàn bộ response của một controller.
 * - Dependency: MessageRouteResult, Message, SLF4J.
 */
public class AuctionMessageDispatcher implements ResponseDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionMessageDispatcher.class);

    @Override
    public void dispatch(MessageRouteResult routeResult) {
        if (routeResult == null) {
            LOGGER.warn("Cannot dispatch null MessageRouteResult.");
            return;
        }

        Message message = routeResult.getMessage();
        if (message == null) {
            LOGGER.warn("Cannot dispatch null client response message.");
            return;
        }

        if (!routeResult.hasHandler()) {
            LOGGER.warn("No client response handler for action: {}", message.getAction());
            return;
        }

        try {
            routeResult.getHandler().handle(message);
        } catch (Exception e) {
            LOGGER.error("Unhandled exception while dispatching response: {}", message.getAction(), e);
        }
    }
}
