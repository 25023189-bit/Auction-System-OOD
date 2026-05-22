package com.auction.client.feature.controllers.auction.seller.form;

import java.time.LocalDate;

public record SellerAuctionFormData(
        String itemName,
        String itemDescription,
        String startingPrice,
        String minimumJoinAmount,
        String bidStep,
        LocalDate startDate,
        String startHour,
        String startMinute,
        String duration,
        String extensionSeconds
) {
}
