package com.auction.client.feature.action;

/**
 * DTO legacy mô tả một tin nhắn chat trong phòng.
 *
 * Vai trò:
 * - Lưu roomId, message và userId cho luồng chat cũ.
 * - Cung cấp getter để handler/presenter cũ đọc dữ liệu.
 *
 * Luồng chính:
 * 1. UI legacy tạo ChatRequest từ nội dung người dùng nhập.
 * 2. Handler đọc dữ liệu và gửi xuống AuctionService.
 *
 * Business rules:
 * - message là nội dung người dùng nhập, chưa bắt buộc trim tại DTO.
 * - roomId/userId chỉ là dữ liệu client-side; server vẫn xác thực ngữ cảnh phòng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: field mutable không final, nhưng không có setter public.
 * - Dependency: package feature.action legacy.
 */
public class ChatRequest {
    private String roomId;
    private String message;
    private String userId;

    public ChatRequest(String roomId, String message, String userId) {
        this.roomId = roomId;
        this.message = message;
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }
}
