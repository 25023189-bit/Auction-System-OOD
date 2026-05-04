package com.auction.client.feature.room;

import com.auction.server.service.AuctionService;

/**
 * ActionHandler xử lý gửi chat trong phòng đấu giá.
 *
 * Vai trò:
 * - Nhận ChatRequest từ controller và chuẩn hóa nội dung.
 * - Gọi AuctionService.sendChat() để gửi tin nhắn lên server.
 *
 * Luồng chính:
 * 1. AuctionController tạo ChatRequest từ ô nhập chat.
 * 2. Handler bỏ qua nội dung rỗng, trim text và gửi message.
 *
 * Business rules:
 * - Không gửi tin nhắn null hoặc chỉ chứa khoảng trắng.
 * - Nội dung gửi lên server là nội dung đã trim.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: dependency final nhưng AuctionService có state client.
 * - Dependency: ActionHandler<ChatRequest>, AuctionService.
 */
public class ChatActionHandler implements ActionHandler<ChatRequest> {
    private final AuctionService auctionService;

    public ChatActionHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void handle(ChatRequest request) {
        // Không gửi tin nhắn rỗng lên server.
        if (request.content() != null && !request.content().trim().isEmpty()) {
            auctionService.sendChat(request.content().trim());
        }
    }
}
