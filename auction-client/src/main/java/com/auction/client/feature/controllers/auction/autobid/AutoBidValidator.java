package com.auction.client.feature.controllers.auction.autobid;

public class AutoBidValidator {
    public AutoBidValidationResult validate(AutoBidFormReader.AutoBidFormData formData) {
        if (formData == null || formData.maxBidText().isBlank() || formData.stepText().isBlank()) {
            return AutoBidValidationResult.invalid("He thong: Vui long nhap du Max Bid va buoc gia!\n");
        }

        try {
            double maxBid = Double.parseDouble(formData.maxBidText());
            double step = Double.parseDouble(formData.stepText());

            if (maxBid <= 0 || step <= 0) {
                return AutoBidValidationResult.invalid("He thong: Vui long nhap du Max Bid va buoc gia!\n");
            }

            return AutoBidValidationResult.valid(maxBid, step);
        } catch (NumberFormatException e) {
            return AutoBidValidationResult.invalid("He thong: Loi nhap lieu, vui long nhap so hop le!\n");
        }
    }

    public record AutoBidValidationResult(boolean valid, double maxBid, double step, String message) {
        private static AutoBidValidationResult valid(double maxBid, double step) {
            return new AutoBidValidationResult(true, maxBid, step, null);
        }

        private static AutoBidValidationResult invalid(String message) {
            return new AutoBidValidationResult(false, 0, 0, message);
        }
    }
}
