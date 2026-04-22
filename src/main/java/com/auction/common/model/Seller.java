package com.auction.common.model;

import java.io.Serial;

public class Seller extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    public Seller() { super(); }

    public Seller(String id, String username, String password, double balance) {
        super(id, username, "SELLER", password, balance);
    }
}
