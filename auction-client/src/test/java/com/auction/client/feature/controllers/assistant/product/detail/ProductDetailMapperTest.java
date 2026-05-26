package com.auction.client.feature.controllers.assistant.product.detail;

import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.ProductDetailResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductDetailMapperTest {
    private final ProductDetailMapper mapper = new ProductDetailMapper();

    @Test
    void mapsProductDetailResponseUsingLiveAuctionValues() {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setTitle("Watch");
        response.setDescription("Limited edition");
        response.setStartPrice(100.0);
        response.setCurrentPrice(2600.0);
        response.setTimeLeftMillis(3_900_000L);
        response.setBase64Image("image-data");
        response.setBidHistory(List.of(
                new BidHistoryDTO("A", 2000.0, "now"),
                new BidHistoryDTO("B", 2600.0, "now")
        ));

        ProductDetailViewModel result = mapper.map(response);

        assertEquals("Watch", result.title());
        assertEquals("Limited edition", result.description());
        assertEquals("2,600 $", result.currentPrice());
        assertEquals("2", result.bidCount());
        assertEquals("1h 5m", result.timeRemaining());
        assertEquals("image-data", result.base64Image());
    }

    @Test
    void mapsNullBidHistoryAndEndedAuction() {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setCurrentPrice(25.0);
        response.setTimeLeftMillis(0);

        ProductDetailViewModel result = mapper.map(response);

        assertEquals("0", result.bidCount());
        assertEquals("Ended", result.timeRemaining());
    }

    @Test
    void mapsAuctionRoomUsingCurrentRatherThanStartingPrice() {
        AuctionRoom room = new AuctionRoom();
        room.setItemName("Camera");
        room.setItemDescription("Used");
        room.setStartingPrice(100.0);
        room.setCurrentPrice(450.0);
        room.setEndTime(LocalDateTime.now().plusMinutes(2));
        room.setBase64Image("photo");

        ProductDetailViewModel result = mapper.map(room);

        assertEquals("450 $", result.currentPrice());
        assertEquals("0", result.bidCount());
        assertEquals("photo", result.base64Image());
    }

    @Test
    void formatsSubHourAndSubMinuteTime() {
        assertEquals("2m 3s", mapper.formatTimeRemaining(123_000L));
        assertEquals("42s", mapper.formatTimeRemaining(42_000L));
        assertEquals("Ended", mapper.formatTimeRemaining(-1));
    }

    @Test
    void mapsErrorsAndUnknownData() {
        assertEquals("Server unavailable", mapper.map("Server unavailable").description());
        assertEquals("Product information not found!", mapper.map(null).description());
        assertEquals("Product information not found!", mapper.map(new Object()).description());
    }
}
