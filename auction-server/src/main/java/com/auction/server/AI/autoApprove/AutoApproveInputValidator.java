package com.auction.server.AI.autoApprove;

public class AutoApproveInputValidator {
    public void validate(AutoApproveListingInput input) throws AutoApproveValidationException {
        if (input == null) {
            throw new AutoApproveValidationException("Auto approve input is required.");
        }
        requireText(input.getTitle(), "title");
        requireText(input.getOrganization(), "organization");
        requireText(input.getDescription(), "description");
        requirePositive(input.getSellerRating(), "seller_rating");
        requireFinite(input.getSellerCompletedRating(), "seller_completed_rating");
        requireFinite(input.getSellerCancelRate(), "seller_cancel_rate");
        requirePositive(input.getStartPrice(), "start_price");
        requirePositive(input.getMinimumJoinAmount(), "minimum_join_amount");
        requirePositive(input.getBidStep(), "bid_step");
        if (input.getStartTime() == null) {
            throw new AutoApproveValidationException("start_time is required.");
        }
        if (input.getDurationMinutes() <= 0) {
            throw new AutoApproveValidationException("duration_minutes must be greater than 0.");
        }
        if (input.getExtensionSeconds() <= 0) {
            throw new AutoApproveValidationException("extension_seconds must be greater than 0.");
        }
    }

    private void requireText(String value, String field) throws AutoApproveValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new AutoApproveValidationException(field + " is required.");
        }
    }

    private void requirePositive(double value, String field) throws AutoApproveValidationException {
        requireFinite(value, field);
        if (value <= 0) {
            throw new AutoApproveValidationException(field + " must be greater than 0.");
        }
    }

    private void requireFinite(double value, String field) throws AutoApproveValidationException {
        if (!Double.isFinite(value)) {
            throw new AutoApproveValidationException(field + " must be a finite number.");
        }
    }
}
