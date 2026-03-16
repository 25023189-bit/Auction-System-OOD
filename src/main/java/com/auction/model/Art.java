package com.auction.model;

public class Art extends Item {
    private String artistName;

    public Art(String id, String name, String description, double startingPrice, String artistName) {
        super(id, name, description, startingPrice);
        this.artistName = artistName;
    }
    @Override
    public void printInfo() {
        System.out.println("[Nghệ thuật] " + getName() + " - Tác giả: " + artistName
                + " - Giá hiện tại: $" + getCurrentHighestPrice());
    }
}