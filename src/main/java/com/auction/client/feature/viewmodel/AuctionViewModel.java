package com.auction.client.feature.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for Auction operations
 */
public class AuctionViewModel {
    private final StringProperty roomId = new SimpleStringProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty currentPrice = new SimpleStringProperty();
    private final StringProperty timeRemaining = new SimpleStringProperty();
    private final StringProperty bidAmount = new SimpleStringProperty();

    public AuctionViewModel() {
    }

    // Getters
    public String getRoomId() {
        return roomId.get();
    }

    public StringProperty roomIdProperty() {
        return roomId;
    }

    public String getItemName() {
        return itemName.get();
    }

    public StringProperty itemNameProperty() {
        return itemName;
    }

    public String getCurrentPrice() {
        return currentPrice.get();
    }

    public StringProperty currentPriceProperty() {
        return currentPrice;
    }

    public String getTimeRemaining() {
        return timeRemaining.get();
    }

    public StringProperty timeRemainingProperty() {
        return timeRemaining;
    }

    public String getBidAmount() {
        return bidAmount.get();
    }

    public StringProperty bidAmountProperty() {
        return bidAmount;
    }

    // Setters
    public void setRoomId(String value) {
        roomId.set(value);
    }

    public void setItemName(String value) {
        itemName.set(value);
    }

    public void setCurrentPrice(String value) {
        currentPrice.set(value);
    }

    public void setTimeRemaining(String value) {
        timeRemaining.set(value);
    }

    public void setBidAmount(String value) {
        bidAmount.set(value);
    }
}
