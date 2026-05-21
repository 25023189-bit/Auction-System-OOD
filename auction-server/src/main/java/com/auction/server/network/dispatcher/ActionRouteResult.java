package com.auction.server.network.dispatcher;

import com.auction.common.dto.Message;
import com.auction.server.handler.ClientActionContext;
import com.auction.server.handler.ClientActionHandler;

/**
 * Kết quả định tuyến một action từ client ở phía server.
 *
 * Vai trò:
 * - Mang handler được router chọn cùng Message và ClientActionContext gốc.
 * - Cho phép router chỉ quyết định đích đến, còn dispatcher chịu trách nhiệm thực thi.
 *
 * Luồng chính:
 * 1. ClientActionRouter tạo ActionRouteResult sau khi kiểm tra action.
 * 2. ClientActionDispatcher nhận result và gọi handler nếu result hợp lệ.
 *
 * Business rules:
 * - handler có thể null khi action không được hỗ trợ; dispatcher sẽ trả UNKNOWN_ACTION.
 * - message và context phải được giữ nguyên để handler xử lý đúng request của client hiện tại.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: immutable sau khi khởi tạo.
 * - Dependency: Message, ClientActionHandler, ClientActionContext.
 */
public final class ActionRouteResult {
    private final ClientActionHandler handler;
    private final Message message;
    private final ClientActionContext context;

    private ActionRouteResult(ClientActionHandler handler, Message message, ClientActionContext context) {
        this.handler = handler;
        this.message = message;
        this.context = context;
    }

    public static ActionRouteResult matched(ClientActionHandler handler, Message message, ClientActionContext context) {
        return new ActionRouteResult(handler, message, context);
    }

    public static ActionRouteResult unmatched(Message message, ClientActionContext context) {
        return new ActionRouteResult(null, message, context);
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public ClientActionHandler getHandler() {
        return handler;
    }

    public Message getMessage() {
        return message;
    }

    public ClientActionContext getContext() {
        return context;
    }
}
