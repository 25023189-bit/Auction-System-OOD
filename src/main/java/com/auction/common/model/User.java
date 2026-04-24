package com.auction.common.model;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable, Entity {
    @Serial
    private static final long serialVersionUID = 1L;

    protected String customerId;
    protected String username;
    protected String password;
    protected String role;
    protected String organization;
    protected double balance;
    protected double sellerReputation = 5.0;
    protected double successfulAuctionRate = 0.0;
    protected double adminCancellationRate = 0.0;

    public User() {}

    public User(String customerId, String password, String role) {
        this.customerId = customerId;
        this.username = customerId;
        this.password = password;
        this.role = role;
        this.organization = null;
        this.balance = 0.0;
    }

    public User(String customerId, String username, String role, String password, double balance) {
        this(customerId, username, role, password, null, balance);
    }

    public User(String customerId, String username, String role, String password, String organization, double balance) {
        this.customerId = customerId;
        this.username = username;
        this.role = role;
        this.password = password;
        this.organization = organization;
        this.balance = balance;
    }

    @Override
    public String getId() {
        return this.customerId;
    }

    @Override
    public String getName() {
        return (this.username != null && !this.username.isEmpty()) ? this.username : this.customerId;
    }

    public String getUsername() {
        return (username != null) ? username : customerId;
    }

    public String getConfirmPassword() {
        return this.password;
    }

    public String getCustomerId() { return customerId; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    public String getOrganization() { return organization; }
    public double getBalance() { return balance; }
    public double getSellerReputation() { return sellerReputation; }
    public double getSuccessfulAuctionRate() { return successfulAuctionRate; }
    public double getAdminCancellationRate() { return adminCancellationRate; }

    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setOrganization(String organization) { this.organization = organization; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setSellerReputation(double sellerReputation) { this.sellerReputation = sellerReputation; }
    public void setSuccessfulAuctionRate(double successfulAuctionRate) { this.successfulAuctionRate = successfulAuctionRate; }
    public void setAdminCancellationRate(double adminCancellationRate) { this.adminCancellationRate = adminCancellationRate; }
}
