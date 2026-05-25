package com.auction.client.feature.controllers.auction.seller.form;

import java.time.LocalDateTime;

public record SellerAuctionFormData(
        String itemName,
        String itemDescription,
        double startingPrice,
        double minimumJoinAmount,
        double bidStep,
        LocalDateTime startTime,
        int durationMinutes,
        int extensionSeconds
) {
}
