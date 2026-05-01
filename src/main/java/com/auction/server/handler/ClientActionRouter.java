package com.auction.server.handler;

import com.auction.common.dto.Message;

import java.util.List;

/**
 * Router điều phối Message từ client tới handler phù hợp.
 *
 * Vai trò:
 * - Giữ danh sách ClientActionHandler đã đăng ký cho một connection.
 * - Tìm handler đầu tiên hỗ trợ action và chuyển Message sang handler đó.
 *
 * Luồng chính:
 * 1. ClientHandler gọi route() sau khi nhận Message hợp lệ từ socket.
 * 2. Router duyệt handlers, gọi handle() ở handler match hoặc trả UNKNOWN_ACTION.
 *
 * Business rules:
 * - Thứ tự handler trong danh sách quyết định handler được chọn khi action bị khai báo trùng.
 * - Action không được hỗ trợ phải trả Message UNKNOWN_ACTION cho client hiện tại.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: danh sách handler được copy bất biến; handler bên trong có thread-safety riêng.
 * - Dependency: ClientActionHandler, ClientActionContext, Message.
 */
public class ClientActionRouter {
    private final List<ClientActionHandler> handlers;

    public ClientActionRouter(List<ClientActionHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public void route(Message message, ClientActionContext context) {
        // Handler đầu tiên nhận action sẽ xử lý và kết thúc luồng.
        for (ClientActionHandler handler : handlers) {
            if (handler.canHandle(message.getAction())) {
                handler.handle(message, context);
                return;
            }
        }

        context.send(new Message(
                "UNKNOWN_ACTION",
                "SERVER",
                "Unsupported action: " + message.getAction()
        ));
    }
}
