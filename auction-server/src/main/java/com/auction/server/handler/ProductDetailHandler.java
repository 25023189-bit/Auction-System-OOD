package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.ProductDetailResponse;
import com.auction.server.service.ProductDetailService;

/**
 * Legacy standalone product detail handler retained for compatibility.
 * The active route is owned by {@link RoomActionHandler}.
 */
public class ProductDetailHandler implements ClientActionHandler {
    private final ProductDetailService detailService;

    public ProductDetailHandler() {
        this(new ProductDetailService());
    }

    ProductDetailHandler(ProductDetailService detailService) {
        this.detailService = detailService;
    }

    @Override
    public boolean canHandle(String action) {
        return "GET_PRODUCT_DETAILS".equals(action);
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        try {
            String roomId = message.getData() == null ? "" : message.getData().toString().trim();
            if (roomId.isEmpty()) {
                context.send(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "Invalid room ID!"));
                return;
            }

            ProductDetailResponse details = detailService.getProductDetails(roomId);
            if (details == null) {
                context.send(new Message("PRODUCT_DETAILS_ERROR", "SERVER", "Auction room details not found!"));
                return;
            }

            context.send(new Message("PRODUCT_DETAILS_SUCCESS", "SERVER", details));
        } catch (Exception e) {
            context.send(new Message("PRODUCT_DETAILS_ERROR", "SERVER", "Unable to load product details."));
        }
    }
}
