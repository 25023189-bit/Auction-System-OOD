package com.auction.client.feature.viewmodel;

/**
 * Model hiển thị cho một card phòng trong lobby.
 *
 * Vai trò:
 * - Giữ dữ liệu tối thiểu để render một card phòng đấu giá.
 * - Format sẵn currentPrice thành displayPrice cho UI.
 *
 * Luồng chính:
 * 1. RoomDisplayMapper tạo model từ AuctionRoom server.
 * 2. LobbyRoomListRenderer/DefaultAuctionCardFactory đọc model để tạo card.
 *
 * Business rules:
 * - displayPrice phải được tạo từ currentPrice tại thời điểm khởi tạo.
 * - Model không chứa quyền thao tác hoặc trạng thái runtime chi tiết của phòng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: immutable sau constructor, các field đều final.
 * - Dependency: DefaultAuctionCardFactory, LobbyRoomListRenderer, RoomDisplayMapper.
 */
public class LobbyRoomDisplayModel {
    private final String roomId;
    private final String itemName;
    private final double currentPrice;
    private final String displayPrice;

    public LobbyRoomDisplayModel(String roomId, String itemName, double currentPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        // Format sẵn để renderer không phải biết cách trình bày tiền.
        this.displayPrice = String.format("%,.0f $", currentPrice);
    }

    public String getRoomId() {
        return roomId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getDisplayPrice() {
        return displayPrice;
    }
}
