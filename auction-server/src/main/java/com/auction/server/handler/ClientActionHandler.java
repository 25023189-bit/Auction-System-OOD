package com.auction.server.handler;

import com.auction.common.dto.Message;

/**
 * Hợp đồng chung cho handler xử lý action gửi từ client.
 *
 * Vai trò:
 * - Định nghĩa cách kiểm tra một action có thuộc handler hay không.
 * - Định nghĩa điểm vào xử lý Message khi router chọn đúng handler.
 *
 * Luồng chính:
 * 1. ClientActionRouter gọi canHandle(action) trên từng handler đã đăng ký.
 * 2. Handler phù hợp nhận handle(message, context) để xử lý nghiệp vụ.
 *
 * Business rules:
 * - Một action chỉ nên được xử lý bởi handler đầu tiên match trong router.
 * - Handler phải trả response hoặc broadcast phù hợp thông qua ClientActionContext.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: Message và ClientActionContext.
 */
public interface ClientActionHandler {
    boolean canHandle(String action);

    void handle(Message message, ClientActionContext context);
}
