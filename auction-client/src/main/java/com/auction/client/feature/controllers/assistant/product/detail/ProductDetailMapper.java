package com.auction.client.feature.controllers.assistant.product.detail;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.ProductDetailResponse;

public class ProductDetailMapper {
    public ProductDetailViewModel map(Object data) {
        if (data instanceof ProductDetailResponse product) {
            return new ProductDetailViewModel(
                    product.getTitle(),
                    product.getDescription(),
                    String.format("%,.0f $", product.getStartPrice())
            );
        }
        if (data instanceof AuctionRoom room) {
            return new ProductDetailViewModel(
                    room.getItemName(),
                    room.getItemDescription(),
                    String.format("%,.0f $", room.getStartingPrice())
            );
        }
        if (data instanceof String message) {
            return new ProductDetailViewModel("Product Detail Information", message, "");
        }
        return new ProductDetailViewModel("Product Detail Information", "Product information not found!", "");
    }
}
