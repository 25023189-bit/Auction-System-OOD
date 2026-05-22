package com.auction.client.feature.controllers.auction.seller.validation;

import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormData;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class SellerAuctionValidator {
    public SellerAuctionValidationResult validate(SellerAuctionFormData data) {
        if (data == null) {
            return SellerAuctionValidationResult.error("Auction information is invalid.");
        }
        if (data.itemName().isBlank()) {
            return SellerAuctionValidationResult.error("Item name cannot be empty.");
        }
        if (data.startDate() == null) {
            return SellerAuctionValidationResult.error("Please select a start date.");
        }

        try {
            double startingPrice = Double.parseDouble(data.startingPrice());
            double minimumJoinAmount = Double.parseDouble(data.minimumJoinAmount());
            double bidStep = Double.parseDouble(data.bidStep());
            int hour = Integer.parseInt(data.startHour());
            int minute = Integer.parseInt(data.startMinute());
            int duration = Integer.parseInt(data.duration());
            int extensionSeconds = data.extensionSeconds().isEmpty()
                    ? 60
                    : Integer.parseInt(data.extensionSeconds());

            if (startingPrice <= 0) {
                return SellerAuctionValidationResult.error("Starting price must be greater than 0.");
            }
            if (minimumJoinAmount >= 0.75 * startingPrice) {
                return SellerAuctionValidationResult.error("Minimum join amount must be < 75% of starting price.");
            }
            if (bidStep <= 0) {
                return SellerAuctionValidationResult.error("Bid step must be greater than 0.");
            }
            if (duration <= 0) {
                return SellerAuctionValidationResult.error("Duration must be greater than 0 minutes.");
            }
            if (extensionSeconds < 60 || extensionSeconds > 120) {
                return SellerAuctionValidationResult.error("Extension Seconds must be between 60 and 120 seconds.");
            }

            LocalDateTime startTime = LocalDateTime.of(data.startDate(), LocalTime.of(hour, minute));
            if (startTime.isBefore(LocalDateTime.now())) {
                return SellerAuctionValidationResult.error("Start time must be now or in the future.");
            }
        } catch (Exception e) {
            return SellerAuctionValidationResult.error("Price, minimum join amount, bid step, time, duration, and extension must be valid numbers.");
        }

        return SellerAuctionValidationResult.ok();
    }
}
