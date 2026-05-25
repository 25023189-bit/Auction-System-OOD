package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SellerTest {

    @Test
    @DisplayName("Test Constructor mặc định")
    void testDefaultConstructor() {
        Seller seller = new Seller();

        // Vì kế thừa từ User, kiểm tra xem các giá trị mặc định của User có được giữ nguyên không
        assertEquals(5.0, seller.getSellerReputation());
        assertEquals(0.0, seller.getSuccessfulAuctionRate());
        assertNull(seller.getUsername());
    }

    @Test
    @DisplayName("Test Constructor có tham số (Tự động gán Role là SELLER)")
    void testParameterizedConstructor() {
        Seller seller = new Seller("SELLER_VIP_01", "ShopCuaHan", "BaoMat123", 99999.0);

        // Kiểm tra các trường được đẩy lên class cha (User) qua từ khóa super()
        assertEquals("SELLER_VIP_01", seller.getCustomerId());
        assertEquals("ShopCuaHan", seller.getUsername());

        // Đảm bảo Role được fix cứng là "SELLER"
        assertEquals("SELLER", seller.getRole(), "Role của Seller bắt buộc phải là SELLER");

        assertEquals("BaoMat123", seller.getPassword());
        assertEquals(99999.0, seller.getBalance());
    }
}