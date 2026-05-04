package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;

/**
 * Handler legacy gửi tin nhắn chat trong phòng đấu giá.
 *
 * Vai trò:
 * - Bọc lời gọi AuctionService.sendChatMessage() cho package action cũ.
 * - Giữ roomId trong lời gọi để tương thích luồng chat legacy.
 *
 * Luồng chính:
 * 1. UI legacy gọi sendChatMessage(roomId, message).
 * 2. Handler kiểm tra service rồi chuyển dữ liệu xuống AuctionService.
 *
 * Business rules:
 * - Chỉ gửi tin khi AuctionService đã được gắn.
 * - Nội dung rỗng không được chặn ở lớp legacy này; caller/server chịu trách nhiệm kiểm tra.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: giữ AuctionService mutable.
 * - Dependency: AuctionService.
 */
public class ChatActionHandler {
    private AuctionService auctionService;

    public ChatActionHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void sendChatMessage(String roomId, String message) {
        if (auctionService != null) {
            // Gửi cả roomId để server biết tin nhắn thuộc phòng nào.
            auctionService.sendChatMessage(roomId, message);
        }
    }
}
