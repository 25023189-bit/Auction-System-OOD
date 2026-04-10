package com.auction.client.viewmodel;

public class LobbyRoomDisplayModel {
    private final String roomId;
    private final String itemName;
    private final double currentPrice;
    private final String displayPrice;

    public LobbyRoomDisplayModel(String roomId, String itemName, double currentPrice) {
        this.roomId = roomId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.displayPrice = String.format("%,.0f $", currentPrice);
    }

    public String getRoomId() { return roomId; }
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getDisplayPrice() { return displayPrice; }
}