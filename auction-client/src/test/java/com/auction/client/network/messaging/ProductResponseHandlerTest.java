package com.auction.client.network.messaging;

import com.auction.client.service.AuctionService;
import com.auction.common.dto.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProductResponseHandlerTest {
    @Test
    void supportsAllProductDetailServerResponses() {
        ProductResponseHandler handler = new ProductResponseHandler(null);

        assertTrue(handler.supports("PRODUCT_DETAILS_SUCCESS"));
        assertTrue(handler.supports("PRODUCT_DETAILS_ERROR"));
        assertTrue(handler.supports("PRODUCT_DETAILS_FAIL"));
        assertFalse(handler.supports("ROOM_LIST"));
    }

    @Test
    void forwardsPayloadToAuctionServiceCallback() {
        AuctionService service = mock(AuctionService.class);
        ProductResponseHandler handler = new ProductResponseHandler(service);

        handler.handle(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "No room"));

        verify(service).fireProductDetailsReceived("No room");
        assertDoesNotThrow(() -> new ProductResponseHandler(null)
                .handle(new Message("PRODUCT_DETAILS_ERROR", "SERVER", "Failure")));
    }
}
