package com.auction.common.model;

import java.io.Serial;
import java.io.Serializable;

public class Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private double startingPrice;

    public Item() {
    }

    public Item(String id, String name, String description, double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    // --- BỔ SUNG ĐỂ FIX LỖI Ở FILE ART.JAVA ---

    // Fix lỗi Art.java gọi getName() ở dòng 14
    public String getName() {
        return name;
    }

    // Fix lỗi Art.java gọi getCurrentHighestPrice() ở dòng 15
    public double getCurrentHighestPrice() {
        return startingPrice;
    }

    // --- CÁC HÀM GETTER CŨ ---
    public String getProductId() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String getProductName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }
}
