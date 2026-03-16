package com.auction.model;

public class Bidder extends User {
    private double balance;

    public Bidder(String id, String username, String email, double balance) {
        super(id, username, email);
        this.balance = balance;
    }

    @Override
    public void displayMenu() {
        System.out.println("Menu cho người đấu giá: Xem sản phẩm, Đặt giá...");
    }
}