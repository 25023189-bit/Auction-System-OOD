package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.network.dispatcher.ActionRouteResult;

import java.util.List;

/**
 * Router chọn handler phù hợp cho Message từ client.
 *
 * Vai trò:
 * - Giữ danh sách ClientActionHandler đã đăng ký cho một connection.
 * - Tìm handler đầu tiên hỗ trợ action và trả về ActionRouteResult cho dispatcher.
 *
 * Luồng chính:
 * 1. ClientHandler gọi route() sau khi nhận Message hợp lệ từ socket.
 * 2. Router duyệt handlers, chỉ tạo route result; ClientActionDispatcher mới gọi handler.
 *
 * Business rules:
 * - Thứ tự handler trong danh sách quyết định handler được chọn khi action bị khai báo trùng.
 * - Action không được hỗ trợ trả về result không có handler để dispatcher xử lý tập trung.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: danh sách handler được copy bất biến; handler bên trong có thread-safety riêng.
 * - Dependency: ClientActionHandler, ClientActionContext, Message, ActionRouteResult.
 */
public class ClientActionRouter {
    private final List<ClientActionHandler> handlers;

    public ClientActionRouter(List<ClientActionHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public ActionRouteResult route(Message message, ClientActionContext context) {
        // Handler đầu tiên nhận action được trả về cho dispatcher thực thi.
        for (ClientActionHandler handler : handlers) {
            if (handler.canHandle(message.getAction())) {
                return ActionRouteResult.matched(handler, message, context);
            }
        }

        return ActionRouteResult.unmatched(message, context);
    }
}
