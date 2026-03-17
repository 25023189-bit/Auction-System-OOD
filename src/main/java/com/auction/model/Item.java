package com.auction.model;

public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private double currentHighestPrice;

    public Item(String id, String name, String description, double startingPrice) {
        super(id);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestPrice = startingPrice;
    }

    public String getName() { return name; }
    public double getCurrentHighestPrice() { return currentHighestPrice; }
    public void setCurrentHighestPrice(double price) { this.currentHighestPrice = price; }

    public abstract void printInfo();
}