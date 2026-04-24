package com.auction.server.service;

import java.time.LocalDateTime;

public class AuctionCreationValidator {
    private String errorMessage = "Auction information is invalid.";

    public boolean validateAuction(String sellerId,
                                   String sellerOrganization,
                                   String itemName,
                                   String itemDesc,
                                   double startingPrice,
                                   double minimumJoinAmount,
                                   double bidStep,
                                   LocalDateTime startTime,
                                   int durationMinutes,
                                   int extensionSeconds,
                                   double sellerReputation,
                                   double successfulAuctionRate,
                                   double adminCancellationRate) {
        if (sellerId == null || sellerId.isBlank()) {
            errorMessage = "Unable to identify the seller creating this auction.";
            return false;
        }
        if (sellerOrganization == null || sellerOrganization.isBlank()) {
            errorMessage = "Seller organization is required to create an auction.";
            return false;
        }
        if (itemName == null || itemName.isBlank()) {
            errorMessage = "Item name is invalid.";
            return false;
        }
        if (itemDesc == null || itemDesc.isBlank()) {
            errorMessage = "Item description is required.";
            return false;
        }
        if (startingPrice <= 0) {
            errorMessage = "Starting price must be greater than 0.";
            return false;
        }
        if (bidStep <= 0) {
            errorMessage = "Bid step must be greater than 0.";
            return false;
        }
        if (!(minimumJoinAmount < startingPrice * 0.75)) {
            errorMessage = "Minimum join amount must be Less than 75% of the starting price.";
            return false;
        }
        if (durationMinutes <= 0) {
            errorMessage = "Duration must be greater than 0 minutes.";
            return false;
        }
        if (extensionSeconds < 1 || extensionSeconds > 120) {
            errorMessage = "Extension must be between 1 and 120 seconds.";
            return false;
        }
        if (startTime == null || startTime.isBefore(LocalDateTime.now())) {
            errorMessage = "Start time must be now or in the future.";
            return false;
        }
        if (sellerReputation < 0.0 || sellerReputation > 5.0) {
            errorMessage = "Seller reputation must be between 0 and 5.";
            return false;
        }
        if (successfulAuctionRate < 0.0 || successfulAuctionRate > 1.0) {
            errorMessage = "Seller successful auction rate must be between 0 and 1.";
            return false;
        }
        if (adminCancellationRate < 0.0 || adminCancellationRate > 1.0) {
            errorMessage = "Seller admin cancellation rate must be between 0 and 1.";
            return false;
        }
        return true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
