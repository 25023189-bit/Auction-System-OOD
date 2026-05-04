package com.auction.client.feature.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel dùng JavaFX Property cho thông tin đấu giá có thể bind với UI.
 *
 * Vai trò:
 * - Lưu roomId, itemName, currentPrice, timeRemaining và bidAmount dạng StringProperty.
 * - Cung cấp getter/property/setter để FXML hoặc presenter bind trực tiếp.
 *
 * Luồng chính:
 * 1. Controller tạo ViewModel khi khởi tạo màn hình đấu giá.
 * 2. UI hoặc presenter đọc property và cập nhật value khi server/user thay đổi.
 *
 * Business rules:
 * - Các field hiển thị dạng String để phù hợp label/text field JavaFX.
 * - ViewModel không tự validate bid hoặc trạng thái phiên.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: JavaFX Property nên cập nhật trên JavaFX Application Thread.
 * - Dependency: SimpleStringProperty, StringProperty.
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
