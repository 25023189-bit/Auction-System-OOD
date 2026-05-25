package com.auction.common.model;

import java.io.Serial;

public class Admin extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    public Admin(String customerId, String password) {
        super(); // Gọi constructor rỗng của class User

        this.setCustomerId(customerId); // FIX: Thêm dòng này để ID không bị null nữa
        this.setUsername(customerId);
        this.setPassword(password);
        this.setRole("ADMIN");
    }
}