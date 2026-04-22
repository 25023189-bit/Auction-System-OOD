package com.auction.common.model;

import java.io.Serial;

public class Admin extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    public Admin(String customerId, String password) {
        super(); // Gọi constructor rỗng của class User

        // Gọi các hàm Setter (Lưu ý: Nếu hàm bên class User của ông tên khác thì đổi lại cho đúng nhé)
        this.setUsername(customerId);
        this.setPassword(password);
        this.setRole("ADMIN");
    }
}
