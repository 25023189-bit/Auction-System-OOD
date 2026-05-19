package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PendingAuctionRequestTest {

    @Test
    @DisplayName("Test Constructor khổng lồ (16 tham số)")
    void testFullParameterizedConstructor() {
        LocalDateTime time = LocalDateTime.now();

        // Nhồi 16 tham số vào Constructor
        PendingAuctionRequest req = new PendingAuctionRequest(
                "REQ_001", "ROOM_99", "ITEM_88", "SELLER_77", "FITA_UET",
                "Laptop Gaming", "Core i9, RTX 4090",
                5000.0, 100.0, 50.0,
                time, 120, 60,
                4.9, 0.95, 0.01
        );

        // Kéo ra kiểm tra 16 Getter
        assertEquals("REQ_001", req.getRequestId());
        assertEquals("ROOM_99", req.getRoomId());
        assertEquals("ITEM_88", req.getItemId());
        assertEquals("SELLER_77", req.getSellerId());
        assertEquals("FITA_UET", req.getSellerOrganization());
        assertEquals("Laptop Gaming", req.getItemName());
        assertEquals("Core i9, RTX 4090", req.getItemDesc());
        assertEquals(5000.0, req.getStartingPrice());
        assertEquals(100.0, req.getMinimumJoinAmount());
        assertEquals(50.0, req.getBidStep());
        assertEquals(time, req.getStartTime());
        assertEquals(120, req.getDurationMinutes());
        assertEquals(60, req.getExtensionSeconds());
        assertEquals(4.9, req.getSellerReputation());
        assertEquals(0.95, req.getSuccessfulAuctionRate());
        assertEquals(0.01, req.getAdminCancellationRate());
    }

    @Test
    @DisplayName("Test Constructor mặc định và 16 hàm Setter")
    void testDefaultConstructorAndSetters() {
        PendingAuctionRequest req = new PendingAuctionRequest();
        LocalDateTime time2 = LocalDateTime.now().plusDays(1);

        // Kiểm tra vài giá trị mặc định ban đầu
        assertNull(req.getRequestId());
        assertEquals(0.0, req.getStartingPrice());
        assertEquals(0, req.getDurationMinutes());

        // Dội bom 16 hàm Setter
        req.setRequestId("REQ_002");
        req.setRoomId("ROOM_100");
        req.setItemId("ITEM_100");
        req.setSellerId("SELLER_100");
        req.setSellerOrganization("VNU");
        req.setItemName("MacBook Air");
        req.setItemDesc("M2 16GB");
        req.setStartingPrice(1200.0);
        req.setMinimumJoinAmount(50.0);
        req.setBidStep(20.0);
        req.setStartTime(time2);
        req.setDurationMinutes(60);
        req.setExtensionSeconds(30);
        req.setSellerReputation(4.5);
        req.setSuccessfulAuctionRate(0.85);
        req.setAdminCancellationRate(0.05);

        // Kiểm chứng kết quả
        assertEquals("REQ_002", req.getRequestId());
        assertEquals("ROOM_100", req.getRoomId());
        assertEquals("ITEM_100", req.getItemId());
        assertEquals("SELLER_100", req.getSellerId());
        assertEquals("VNU", req.getSellerOrganization());
        assertEquals("MacBook Air", req.getItemName());
        assertEquals("M2 16GB", req.getItemDesc());
        assertEquals(1200.0, req.getStartingPrice());
        assertEquals(50.0, req.getMinimumJoinAmount());
        assertEquals(20.0, req.getBidStep());
        assertEquals(time2, req.getStartTime());
        assertEquals(60, req.getDurationMinutes());
        assertEquals(30, req.getExtensionSeconds());
        assertEquals(4.5, req.getSellerReputation());
        assertEquals(0.85, req.getSuccessfulAuctionRate());
        assertEquals(0.05, req.getAdminCancellationRate());
    }
}