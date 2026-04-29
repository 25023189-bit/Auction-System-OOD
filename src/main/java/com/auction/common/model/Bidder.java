package com.auction.common.model;

import java.io.Serial;

public class Bidder extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    public Bidder() {
        super();
    }

    public Bidder(String id, String username, String password, double balance) {
        super(id, username, "BIDDER", password, balance);
    }

    // Thêm các hàm phụ trợ nếu ClientHandler yêu cầu
    public void deposit(double amount) {
        this.balance += amount;
    }
}
