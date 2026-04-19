package com.auction.common.model;

public class Bidder extends User {
    public Bidder() { super(); }

    public Bidder(String id, String username, String password, double balance) {
        super(id, username, "BIDDER", password, balance);
    }

    // Thêm các hàm phụ trợ nếu ClientHandler yêu cầu
    public void deposit(double amount) { this.balance += amount; }
}