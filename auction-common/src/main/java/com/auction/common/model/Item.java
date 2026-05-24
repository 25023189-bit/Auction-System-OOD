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
    private String base64Image;

    public Item() {
    }

    public Item(String id, String name, String description, double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public Item(String id, String name, String description, double startingPrice, String base64Image) {
        this(id, name, description, startingPrice);
        this.base64Image = base64Image;
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

    public String getBase64Image() { return base64Image; }

    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
}
