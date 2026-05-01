package com.auction.client.feature.viewmodel;

import java.time.LocalDateTime;

/**
 * ViewModel biểu diễn dữ liệu phòng đấu giá đang hiển thị.
 *
 * Vai trò:
 * - Lưu các trường cần render cho màn hình auction room.
 * - Chuẩn bị displayPrice khi currentPrice thay đổi để UI dùng trực tiếp.
 *
 * Luồng chính:
 * 1. Controller/binder nạp dữ liệu room từ server hoặc session vào ViewModel.
 * 2. Presenter đọc getter để cập nhật label/control tương ứng.
 *
 * Business rules:
 * - setCurrentPrice() phải đồng bộ cả giá số và chuỗi giá đã format.
 * - owner cho biết user hiện tại có quyền thao tác owner như đóng phiên hay không.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: field mutable, không có synchronization/property binding.
 * - Dependency: LocalDateTime và các presenter/controller dùng ViewModel.
 */
public class AuctionRoomViewModel {
    private String roomId;
    private String itemName;
    private String itemDescription;
    private double currentPrice;
    private String displayPrice;
    private String sellerName;
    private boolean owner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        // Cập nhật cả giá dạng số và chuỗi đã format để UI có thể dùng trực tiếp.
        this.currentPrice = currentPrice;
        this.displayPrice = String.format("%,.0f $", currentPrice);
    }

    public String getDisplayPrice() {
        return displayPrice;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public boolean isOwner() {
        return owner;
    }
}
