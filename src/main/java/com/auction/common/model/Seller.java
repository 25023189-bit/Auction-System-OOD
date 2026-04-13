package com.auction.common.model;

public class Seller extends User {
    public Seller() { super(); }

    public Seller(String id, String username, String password, double balance) {
        super(id, username, "SELLER", password, balance);
    }
}