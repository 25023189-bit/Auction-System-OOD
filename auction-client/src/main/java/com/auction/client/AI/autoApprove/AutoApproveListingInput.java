package com.auction.client.AI.autoApprove;

import com.auction.common.model.PendingAuctionRequest;

import java.time.LocalDateTime;

public class AutoApproveListingInput {
    public static final int SCHEMA_VERSION = 1;

    private final String title;
    private final String category;
    private final String organization;
    private final String description;
    private final double sellerRating;
    private final double sellerCompletedRating;
    private final double sellerCancelRate;
    private final double startPrice;
    private final double minimumJoinAmount;
    private final double bidStep;
    private final LocalDateTime startTime;
    private final int durationMinutes;
    private final int extensionSeconds;

    public AutoApproveListingInput(
            String title,
            String category,
            String organization,
            String description,
            double sellerRating,
            double sellerCompletedRating,
            double sellerCancelRate,
            double startPrice,
            double minimumJoinAmount,
            double bidStep,
            LocalDateTime startTime,
            int durationMinutes,
            int extensionSeconds
    ) {
        this.title = title;
        this.category = category;
        this.organization = organization;
        this.description = description;
        this.sellerRating = sellerRating;
        this.sellerCompletedRating = sellerCompletedRating;
        this.sellerCancelRate = sellerCancelRate;
        this.startPrice = startPrice;
        this.minimumJoinAmount = minimumJoinAmount;
        this.bidStep = bidStep;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.extensionSeconds = extensionSeconds;
    }

    public static AutoApproveListingInput fromPendingRequest(PendingAuctionRequest request) {
        return new AutoApproveListingInput(
                request.getItemName(),
                "",
                request.getSellerOrganization(),
                request.getItemDesc(),
                request.getSellerReputation(),
                request.getSuccessfulAuctionRate(),
                request.getAdminCancellationRate(),
                request.getStartingPrice(),
                request.getMinimumJoinAmount(),
                request.getBidStep(),
                request.getStartTime(),
                request.getDurationMinutes(),
                request.getExtensionSeconds()
        );
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDescription() {
        return description;
    }

    public double getSellerRating() {
        return sellerRating;
    }

    public double getSellerCompletedRating() {
        return sellerCompletedRating;
    }

    public double getSellerCancelRate() {
        return sellerCancelRate;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getMinimumJoinAmount() {
        return minimumJoinAmount;
    }

    public double getBidStep() {
        return bidStep;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getExtensionSeconds() {
        return extensionSeconds;
    }
}
