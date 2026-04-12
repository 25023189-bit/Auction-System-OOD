package com.auction.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletTransaction {
    private int transactionId;
    private int walletId;
    private String customerId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String referenceId;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public WalletTransaction() {}

    public WalletTransaction(int walletId, String customerId, TransactionType type, BigDecimal amount) {
        this.walletId = walletId;
        this.customerId = customerId;
        this.transactionType = type;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    // Bác thêm Getters/Setters ở đây nhé...
}