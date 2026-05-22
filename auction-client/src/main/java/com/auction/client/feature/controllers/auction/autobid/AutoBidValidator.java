package com.auction.client.feature.controllers.auction.autobid;

public class AutoBidValidator {
    public boolean isValid(double maxBid, double step) {
        return maxBid > 0 && step > 0;
    }
}
