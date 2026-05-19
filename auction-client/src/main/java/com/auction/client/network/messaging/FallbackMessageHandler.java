package com.auction.client.network.messaging;

import com.auction.common.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handler dự phòng gom các fallback handler nhỏ hơn.
 *
 * Vai trò:
 * - Nhận mọi action chưa được handler chính xử lý.
 * - Duyệt fallbackHandlers và chuyển message cho handler đầu tiên hỗ trợ action.
 *
 * Luồng chính:
 * 1. AuctionMessageRouter gọi handle(message) khi không có primary handler match.
 * 2. FallbackMessageHandler duyệt danh sách fallback, gọi handler phù hợp hoặc log warning.
 *
 * Business rules:
 * - supports() luôn trả true để router có thể dùng làm fallback cuối.
 * - Action không có fallback phải được log để dễ phát hiện thiếu handler.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: danh sách fallback được copy bất biến khi tạo handler.
 * - Dependency: MessageHandler, Message, List, SLF4J.
 */
public class FallbackMessageHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMessageHandler.class);

    private final List<MessageHandler> fallbackHandlers;

    public FallbackMessageHandler(List<MessageHandler> fallbackHandlers) {
        this.fallbackHandlers = List.copyOf(fallbackHandlers);
    }

    @Override
    public boolean supports(String action) {
        return true;
    }

    @Override
    public void handle(Message message) {
        // Duyệt các fallback nhỏ hơn để mỗi lớp chỉ xử lý một nhóm nghiệp vụ.
        for (MessageHandler handler : fallbackHandlers) {
            if (handler.supports(message.getAction())) {
                handler.handle(message);
                return;
            }
        }

        LOGGER.warn("No handler for action: {}", message.getAction());
    }
}
