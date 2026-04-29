package com.auction.client.feature.viewmodel;

/**
 * Model hiển thị cho một card phòng trong lobby.
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

<<<<<<< HEAD
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
=======
    public String getRoomId() { return roomId; }
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getDisplayPrice() { return displayPrice; }
}
>>>>>>> 79695510de950987573eb4278b356292c3d972f3
