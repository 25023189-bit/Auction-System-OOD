package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.ProductDetailResponse;
import com.auction.server.service.ProductDetailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductDetailHandlerTest {
    private final ProductDetailService service = mock(ProductDetailService.class);
    private final ClientActionContext context = mock(ClientActionContext.class);
    private final ProductDetailHandler handler = new ProductDetailHandler(service);

    @Test
    void failsWhenRoomIdIsMissing() {
        handler.handle(new Message("GET_PRODUCT_DETAILS", null), context);

        verify(context).send(org.mockito.ArgumentMatchers.argThat(
                message -> "PRODUCT_DETAILS_FAIL".equals(message.getAction())));
    }

    @Test
    void reportsMissingProductData() {
        when(service.getProductDetails("missing")).thenReturn(null);

        handler.handle(new Message("GET_PRODUCT_DETAILS", "missing"), context);

        verify(context).send(org.mockito.ArgumentMatchers.argThat(
                message -> "PRODUCT_DETAILS_ERROR".equals(message.getAction())));
    }

    @Test
    void returnsServiceProductData() {
        ProductDetailResponse response = new ProductDetailResponse();
        when(service.getProductDetails("room")).thenReturn(response);

        handler.handle(new Message("GET_PRODUCT_DETAILS", "room"), context);

        verify(context).send(org.mockito.ArgumentMatchers.argThat(
                message -> "PRODUCT_DETAILS_SUCCESS".equals(message.getAction())
                        && message.getData() == response));
    }
}
