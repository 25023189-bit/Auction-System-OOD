package com.auction.client.autoApprove;

public class AutoApproveOutputValidator {
    public boolean parseDecision(String rawValue) throws AutoApproveValidationException {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new AutoApproveValidationException("output_ap.json is empty.");
        }

        String normalized = rawValue.trim();
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }

        throw new AutoApproveValidationException("output_ap.json must contain only true or false.");
    }
}
