package com.auction.client.feature.controllers.auction.seller.validation;

import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormData;

import java.time.LocalDateTime;

public class SellerAuctionValidator {
    public SellerAuctionValidationResult validate(SellerAuctionFormData data) {
        if (data == null) {
            return SellerAuctionValidationResult.error("Auction information is invalid.");
        }
        if (data.itemName().isBlank()) {
            return SellerAuctionValidationResult.error("Item name cannot be empty.");
        }
        if (data.startingPrice() <= 0) {
            return SellerAuctionValidationResult.error("Starting price must be greater than 0.");
        }
        SellerAuctionValidationResult joinAmountResult = validateJoinAmount(data);
        if (!joinAmountResult.valid()) {
            return joinAmountResult;
        }
        if (data.bidStep() <= 0) {
            return SellerAuctionValidationResult.error("Bid step must be greater than 0.");
        }
        SellerAuctionValidationResult durationResult = validateDuration(data);
        if (!durationResult.valid()) {
            return durationResult;
        }
        SellerAuctionValidationResult extensionResult = validateExtensionSeconds(data);
        if (!extensionResult.valid()) {
            return extensionResult;
        }
        if (data.startTime().isBefore(LocalDateTime.now())) {
            return SellerAuctionValidationResult.error("Start time must be now or in the future.");
        }

        return SellerAuctionValidationResult.ok();
    }

    public SellerAuctionValidationResult validateJoinAmount(SellerAuctionFormData data) {
        if (data.minimumJoinAmount() >= 0.75 * data.startingPrice()) {
            return SellerAuctionValidationResult.error("Error: Minimum join amount must be < 75% of starting price");
        }
        return SellerAuctionValidationResult.ok();
    }

    public SellerAuctionValidationResult validateDuration(SellerAuctionFormData data) {
        if (data.durationMinutes() <= 0) {
            return SellerAuctionValidationResult.error("Error: Duration must be greater than 0 minutes");
        }
        return SellerAuctionValidationResult.ok();
    }

    public SellerAuctionValidationResult validateExtensionSeconds(SellerAuctionFormData data) {
        if (data.extensionSeconds() < 60 || data.extensionSeconds() > 120) {
            return SellerAuctionValidationResult.error("Error: Extension Seconds must be between 60 and 120 seconds!");
        }
        return SellerAuctionValidationResult.ok();
    }
}
