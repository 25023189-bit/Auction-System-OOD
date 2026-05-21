package com.auction.server.network.dispatcher;

import com.auction.common.dto.Message;
import com.auction.server.handler.ClientActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher thực thi ActionRouteResult ở phía server.
 *
 * Vai trò:
 * - Validate kết quả route trước khi gọi ClientActionHandler.
 * - Gửi UNKNOWN_ACTION khi router không tìm thấy handler phù hợp.
 * - Ghi log lỗi phát sinh trong quá trình gọi handler.
 *
 * Luồng chính:
 * 1. ClientHandler gọi dispatch(result) sau khi router trả ActionRouteResult.
 * 2. Dispatcher gọi handler.handle(message, context) nếu có handler hợp lệ.
 *
 * Business rules:
 * - Không gọi handler khi message/context null.
 * - Action không hỗ trợ phải trả response về đúng client hiện tại.
 *
 * Ghi chú kỹ thuật:
 * - Stateless, có thể dùng lại cho nhiều ClientHandler.
 * - Dependency: ActionRouteResult, Message, ClientActionContext, SLF4J.
 */
public class ClientActionDispatcher implements ActionDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientActionDispatcher.class);

    @Override
    public void dispatch(ActionRouteResult routeResult) {
        if (routeResult == null) {
            LOGGER.warn("Cannot dispatch null ActionRouteResult.");
            return;
        }

        Message message = routeResult.getMessage();
        ClientActionContext context = routeResult.getContext();
        if (message == null || context == null) {
            LOGGER.warn("Cannot dispatch action because message or context is null.");
            return;
        }

        if (!routeResult.hasHandler()) {
            context.send(new Message(
                    "UNKNOWN_ACTION",
                    "SERVER",
                    "Unsupported action: " + message.getAction()
            ));
            return;
        }

        try {
            routeResult.getHandler().handle(message, context);
        } catch (Exception e) {
            LOGGER.error("Unhandled exception while dispatching action: {}", message.getAction(), e);
            context.send(new Message("SERVER_ERROR", "SERVER", "Server error while processing action."));
        }
    }
}
