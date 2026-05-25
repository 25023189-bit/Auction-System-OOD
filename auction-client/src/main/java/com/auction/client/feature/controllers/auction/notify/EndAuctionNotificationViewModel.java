package com.auction.client.feature.controllers.auction.notify;

public class EndAuctionNotificationViewModel {
    private final String auctionId;
    private final String itemName;
    private final double finalPrice;
    private final String winnerUsername;
    private final String currentUsername;
    private final String currentUserRole;
    private final AuctionEndReason endReason;
    private final EndAuctionResultType resultType;
    private final boolean hasWinner;
    private final boolean transactionApplied;
    private final String messageTitle;
    private final String messageBody;
    private final String nextActionHint;

    public EndAuctionNotificationViewModel(
            String auctionId,
            String itemName,
            double finalPrice,
            String winnerUsername,
            String currentUsername,
            String currentUserRole,
            AuctionEndReason endReason,
            EndAuctionResultType resultType,
            boolean hasWinner,
            boolean transactionApplied,
            String messageTitle,
            String messageBody,
            String nextActionHint
    ) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
        this.winnerUsername = winnerUsername;
        this.currentUsername = currentUsername;
        this.currentUserRole = currentUserRole;
        this.endReason = endReason;
        this.resultType = resultType;
        this.hasWinner = hasWinner;
        this.transactionApplied = transactionApplied;
        this.messageTitle = messageTitle;
        this.messageBody = messageBody;
        this.nextActionHint = nextActionHint;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public AuctionEndReason getEndReason() {
        return endReason;
    }

    public EndAuctionResultType getResultType() {
        return resultType;
    }

    public boolean hasWinner() {
        return hasWinner;
    }

    public boolean isTransactionApplied() {
        return transactionApplied;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getNextActionHint() {
        return nextActionHint;
    }
}
