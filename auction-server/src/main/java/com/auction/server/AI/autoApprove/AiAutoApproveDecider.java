package com.auction.server.AI.autoApprove;

import com.auction.common.model.PendingAuctionRequest;
import com.auction.server.handler.SellerActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AiAutoApproveDecider implements SellerActionHandler.AutoApproveDecider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiAutoApproveDecider.class);

    private final AuctionAiAutoApproveConnector connector;

    public AiAutoApproveDecider(AuctionAiAutoApproveConnector connector) {
        this.connector = Objects.requireNonNull(connector);
    }

    @Override
    public synchronized boolean shouldApprove(PendingAuctionRequest request) {
        if (request == null) {
            return false;
        }

        try {
            AutoApproveListingInput input = AutoApproveListingInput.fromPendingRequest(request);
            return connector.requestDecision(input);
        } catch (Exception e) {
            LOGGER.warn(
                    "AI auto approve failed. requestId={}, itemName={}, fallback=manual approval, reason={}",
                    request.getRequestId(),
                    request.getItemName(),
                    e.getMessage(),
                    e
            );
            return false;
        }
    }
}
