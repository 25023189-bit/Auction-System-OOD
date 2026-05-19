package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    @DisplayName("Test Constructor có tham số và chức năng tự sinh thời gian bid")
    void testParameterizedConstructor() {
        BidTransaction tx = new BidTransaction("ROOM_01", "USER_123", 150000.0, 1);

        assertEquals("ROOM_01", tx.getAuctionId());
        assertEquals("USER_123", tx.getBidderId());
        assertEquals(150000.0, tx.getBidAmount());
        assertEquals(1, tx.getBidRank());

        // Kiểm tra thời gian tự động sinh ra có đúng định dạng dd/MM/yyyy HH:mm:ss không
        String autoTime = tx.getBidTime();
        assertNotNull(autoTime, "Thời gian đặt giá không được để trống");
        assertTrue(autoTime.contains("/"), "Phải chứa dấu / cho ngày tháng");
        assertTrue(autoTime.contains(":"), "Phải chứa dấu : cho giờ phút");
        assertEquals(19, autoTime.length(), "Độ dài chuỗi format chuẩn phải là 19 ký tự");
    }

    @Test
    @DisplayName("Test toàn bộ Setter và Getter còn lại")
    void testGettersAndSetters() {
        BidTransaction tx = new BidTransaction();

        // Bơm dữ liệu
        tx.setTransactionId(999);
        tx.setAuctionId("A_NEW");
        tx.setBidderId("B_NEW");
        tx.setBidAmount(999.9);
        tx.setBidRank(5);
        tx.setBidTime("19/05/2026 02:15:00");
        tx.setHighest(true);

        // Kiểm chứng dữ liệu
        assertEquals(999, tx.getTransactionId());
        assertEquals("A_NEW", tx.getAuctionId());
        assertEquals("B_NEW", tx.getBidderId());
        assertEquals(999.9, tx.getBidAmount());
        assertEquals(5, tx.getBidRank());
        assertEquals("19/05/2026 02:15:00", tx.getBidTime());
        assertTrue(tx.isHighest());
    }
}