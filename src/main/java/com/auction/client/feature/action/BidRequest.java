package com.auction.client.feature.action;

/**
 * DTO legacy mô tả một yêu cầu đặt giá.
 *
 * Vai trò:
 * - Lưu roomId, số tiền bid và userId trong luồng action cũ.
 * - Cung cấp getter cho presenter/handler cũ đọc dữ liệu.
 *
 * Luồng chính:
 * 1. UI legacy tạo BidRequest khi người dùng nhập bid.
 * 2. Handler đọc getter để gửi hoặc xử lý yêu cầu đặt giá.
 *
 * Business rules:
 * - amount là số tiền bid đã parse từ UI.
 * - roomId/userId chỉ là dữ liệu client-side; server vẫn xác thực phiên và người dùng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: field mutable không final, nhưng không có setter public.
 * - Dependency: package feature.action legacy.
 */
public class BidRequest {
    private String roomId;
    private double amount;
    private String userId;

    public BidRequest(String roomId, double amount, String userId) {
        this.roomId = roomId;
        this.amount = amount;
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public double getAmount() {
        return amount;
    }

    public String getUserId() {
        return userId;
    }
}
