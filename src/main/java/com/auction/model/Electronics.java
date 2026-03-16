package com.auction.model;


public class Electronics extends Item {
    private int warrantyMonths;

    public Electronics(String id, String name, String description, double startingPrice, int warrantyMonths) {
        super(id, name, description, startingPrice);
        this.warrantyMonths = warrantyMonths;
    }
    @Override
    public void printInfo() {
        System.out.println("[Điện tử] " + getName() + " - Giá hiện tại: $" + getCurrentHighestPrice()
                + " - Bảo hành: " + warrantyMonths + " tháng");
    }
}