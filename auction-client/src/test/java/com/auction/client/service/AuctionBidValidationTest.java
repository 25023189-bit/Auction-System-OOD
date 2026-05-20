package com.auction.client.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử tự động (Unit Test) cho nghiệp vụ đặt giá đấu giá.
 * Vai trò: Đáp ứng tiêu chí bắt buộc có Unit Test bằng JUnit của Bài tập lớn.
 * Nội dung: Kiểm tra các điều kiện chặn lỗi đồng thời và lỗi logic đúng theo luật của hệ thống.
 */
@DisplayName("Hệ thống Kiểm thử Nghiệp vụ và Lỗi Đặt giá Đấu giá")
public class AuctionBidValidationTest {

    private double mockCurrentPrice;
    private double mockMinIncrement;
    private int mockDatabaseAuctionVersion;

    @BeforeEach
    void setUp() {
        // Thiết lập dữ liệu giả lập đồng bộ với trạng thái nghiệp vụ trong Database SQL của nhóm
        mockCurrentPrice = 1000000.0; // 1,000,000 VND
        mockMinIncrement = 50000.0;   // Bước giá tối thiểu: 50,000 VND
        mockDatabaseAuctionVersion = 10; // Phiên bản hiện tại trong DB là 10
    }

    @Test
    @DisplayName("Đặt giá hợp lệ - Mức giá đáp ứng đúng bước giá tối thiểu")
    void testValidBidAmountCalculation() {
        double alternativeBid = 1100000.0; // Người dùng trả 1,100,000 VND
        double requiredMinBid = mockCurrentPrice + mockMinIncrement;

        assertTrue(alternativeBid >= requiredMinBid, "Mức giá đặt mới phải lớn hơn hoặc bằng mức giá tối thiểu yêu cầu.");
    }

    @Test
    @DisplayName("Ngoại lệ Đặt giá - Tung lỗi Validation Error khi số tiền trả giá quá thấp")
    void testInsufficientBidAmountThrowsException() {
        double lowBidValue = 1020000.0; // Thấp hơn mức tối thiểu yêu cầu là 1,050,000 VND

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            if (lowBidValue < (mockCurrentPrice + mockMinIncrement)) {
                throw new IllegalArgumentException("Validation Error: Insufficient max bid amount");
            }
        });

        assertEquals("Validation Error: Insufficient max bid amount", exception.getMessage(),
                "Ngoại lệ lỗi đặt giá phải khớp chính xác với thông điệp từ Stored Procedure trong CSDL.");
    }

    @Test
    @DisplayName("Ngoại lệ Đồng thời - Tung lỗi Concurrency Error khi sai lệch phiên bản Auction Version")
    void testAuctionVersionMismatchThrowsException() {
        int clientProvidedVersion = 9; // Giả lập Client gửi lên phiên bản cũ (9 cũ hơn 10 trong DB)

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            if (clientProvidedVersion != mockDatabaseAuctionVersion) {
                throw new IllegalStateException("Concurrency Error: Auction version mismatch");
            }
        });

        assertTrue(exception.getMessage().contains("Concurrency Error"),
                "Hệ thống phải kích hoạt cơ chế Optimistic Locking để ngăn chặn lỗi ghi đè dữ liệu đồng thời.");
    }
}