package com.auction.client.feature.controllers.assistant.product.detail;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.ProductDetailResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

public class ProductDetailMapper {
    public ProductDetailViewModel map(Object data) {
        if (data instanceof ProductDetailResponse product) {
            return new ProductDetailViewModel(
                    product.getTitle(),
                    product.getDescription(),
                    formatPrice(product.getCurrentPrice()),
                    String.valueOf(product.getBidHistory() == null ? 0 : product.getBidHistory().size()),
                    formatTimeRemaining(product.getTimeLeftMillis()),
                    product.getBase64Image()
            );
        }
        if (data instanceof AuctionRoom room) {
            return new ProductDetailViewModel(
                    room.getItemName(),
                    room.getItemDescription(),
                    formatPrice(room.getCurrentPrice()),
                    "0",
                    formatTimeRemaining(calculateTimeRemaining(room.getEndTime())),
                    room.getBase64Image()
            );
        }
        if (data instanceof String message) {
            return emptyDetail(message);
        }
        return emptyDetail("Product information not found!");
    }

    String formatTimeRemaining(long millis) {
        if (millis <= 0) {
            return "Ended";
        }

        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%dh %dm", hours, minutes);
        }
        if (minutes > 0) {
            return String.format(Locale.ROOT, "%dm %ds", minutes, remainingSeconds);
        }
        return String.format(Locale.ROOT, "%ds", seconds);
    }

    private ProductDetailViewModel emptyDetail(String description) {
        return new ProductDetailViewModel("Product Detail Information", description, "", "", "", "");
    }

    private String formatPrice(double currentPrice) {
        return String.format(Locale.US, "%,.0f $", currentPrice);
    }

    private long calculateTimeRemaining(LocalDateTime endTime) {
        if (endTime == null) {
            return 0;
        }
        return Math.max(0L, Duration.between(LocalDateTime.now(), endTime).toMillis());
    }
}
