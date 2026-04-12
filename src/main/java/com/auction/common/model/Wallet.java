package com.auction.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet {
    private int walletId;
    private String customerId; // Format BD5XXXXX [cite: 153]
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal availableBalance; // Field tự động tính (balance - frozenBalance)
    private BigDecimal totalDeposited;
    private BigDecimal totalWithdrawn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Wallet() {}

    // Getter và Setter
    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getFrozenBalance() { return frozenBalance; }
    public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }

    public BigDecimal getAvailableBalance() { return balance.subtract(frozenBalance); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}