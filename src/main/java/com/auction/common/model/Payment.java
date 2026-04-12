package com.auction.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private String auctionId;
    private String sellerId;
    private String buyerId;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public Payment() {}

    public Payment(String auctionId, String sellerId, String buyerId, BigDecimal amount, PaymentStatus status) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.amount = amount;
        this.status = status;
        this.platformFee = amount.multiply(new BigDecimal("0.05"));
        this.createdAt = LocalDateTime.now();
    }

    // Bác thêm Getters/Setters ở đây nhé...
}