package com.auction.client.feature.room;

import com.auction.client.service.AuctionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuctionCloseHandlerTest {

    @Test
    @DisplayName("Test không cho phép đóng phòng nếu mã phòng rỗng hoặc null")
    void testCloseRoom_BlankOrNull() {
        AuctionService mockService = mock(AuctionService.class);
        AuctionCloseHandler handler = new AuctionCloseHandler(mockService);

        handler.closeRoom(null);
        handler.closeRoom("");
        handler.closeRoom("   ");

        verify(mockService, never()).closeAuction(anyString());
    }

    @Test
    @DisplayName("Test gửi lệnh đóng phòng thành công với mã phòng hợp lệ")
    void testCloseRoom_Valid() {
        AuctionService mockService = mock(AuctionService.class);
        AuctionCloseHandler handler = new AuctionCloseHandler(mockService);

        handler.closeRoom("AU10001");

        verify(mockService, times(1)).closeAuction("AU10001");
    }
}
