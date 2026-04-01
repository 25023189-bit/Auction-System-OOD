package com.auction.common.model;

// Nếu Art kế thừa Item và chung thư mục thì KHÔNG cần import Item.
// Nhưng nếu cần dùng Serializable:
import java.io.Serializable;

public class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private double currentHighestPrice;


    public Item() {}

    // Constructor có tham số để dùng trong file Art.java
    public Item(String id, String name, String description, double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currentHighestPrice = startingPrice;
    }

    // Các hàm Getter để thằng Art có thể gọi được (getName, getCurrentHighestPrice)
    public String getName() {
        return name;
    }

    public double getCurrentHighestPrice() {
        return currentHighestPrice;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    // Hàm Setter để cập nhật giá khi có người đấu giá mới
    public void setCurrentHighestPrice(double currentHighestPrice) {
        this.currentHighestPrice = currentHighestPrice;
    }
}