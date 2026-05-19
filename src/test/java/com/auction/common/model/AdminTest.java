package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminTest {

    @Test
    @DisplayName("Test Constructor của Admin thiết lập đúng Role và thông tin")
    void testAdminConstructor() {
        // Khởi tạo Admin với customerId và password
        Admin admin = new Admin("ADMIN_VIP_01", "AdminPassword123!");

        // Kiểm tra đúng những gì Constructor đã set (Sau khi đã fix code gốc)
        assertEquals("ADMIN_VIP_01", admin.getCustomerId(), "ID phải được gán chuẩn xác");
        assertEquals("ADMIN_VIP_01", admin.getUsername(), "Username cũng được gán bằng ID");
        assertEquals("AdminPassword123!", admin.getPassword());
        assertEquals("ADMIN", admin.getRole());
    }
}