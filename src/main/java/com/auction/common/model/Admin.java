package com.auction.common.model;

public class Admin extends User {
    public Admin(String id, String username, String password, double balance) {
        //Cố định role là "ADMIN"
        super(id, username, "ADMIN", password, balance);
    }

    // Thêm các thuộc tính riêng của Admin ở đây nếu muốn sau này
}