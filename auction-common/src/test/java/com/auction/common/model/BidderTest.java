package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BidderTest {

    @Test
    @DisplayName("Test Constructor mặc định")
    void testDefaultConstructor() {
        Bidder bidder = new Bidder();

        // Kế thừa từ User, test sương sương xem khởi tạo rỗng có hoạt động không
        assertNull(bidder.getUsername());
        assertEquals(0.0, bidder.getBalance());
    }

    @Test
    @DisplayName("Test Constructor có tham số (Tự động gán Role là BIDDER)")
    void testParameterizedConstructor() {
        Bidder bidder = new Bidder("BIDDER_01", "ToBaoHan", "MatKhauSieuCap", 50000.0);

        // Kiểm tra các trường được đẩy lên class cha (User)
        assertEquals("BIDDER_01", bidder.getCustomerId());
        assertEquals("ToBaoHan", bidder.getUsername());

        // Đảm bảo Role được fix cứng là "BIDDER"
        assertEquals("BIDDER", bidder.getRole(), "Role của Bidder bắt buộc phải là BIDDER");

        assertEquals("MatKhauSieuCap", bidder.getPassword());
        assertEquals(50000.0, bidder.getBalance());
    }

    @Test
    @DisplayName("Test chức năng nạp tiền (deposit)")
    void testDeposit() {
        // Khởi tạo tài khoản với số dư ban đầu là 1000
        Bidder bidder = new Bidder("BIDDER_02", "DaiGiaTienTy", "Secret123", 1000.0);

        // Nạp thêm 2500
        bidder.deposit(2500.0);

        // Kiểm tra số dư tổng (1000 + 2500 = 3500)
        assertEquals(3500.0, bidder.getBalance(), "Số dư sau khi deposit phải được cộng dồn chính xác");

        // Nạp thêm lần nữa
        bidder.deposit(500.5);
        assertEquals(4000.5, bidder.getBalance());
    }
}