package com.auction.client.network.messaging;

import com.auction.client.service.AuctionService;
import com.auction.common.dto.Message;

public class ProductResponseHandler implements MessageHandler {
    private final AuctionService auctionService;

    public ProductResponseHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public boolean supports(String action) {
        // Chỉ nhận 2 loại tin nhắn này từ Server
        return "PRODUCT_DETAILS_SUCCESS".equals(action) || "PRODUCT_DETAILS_ERROR".equals(action);
    }

    @Override
    public void handle(Message message) {
        // Lấy dữ liệu (AuctionRoom hoặc chuỗi báo lỗi)
        Object data = message.getData();

        // Bắn dữ liệu này vào callback để ProductViewController tự động cập nhật UI
        if (auctionService != null) {
            auctionService.fireProductDetailsReceived(data);
        }
    }
}
