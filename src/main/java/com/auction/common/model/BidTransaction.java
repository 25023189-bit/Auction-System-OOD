package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidTransaction implements Serializable {
    private int transactionId; // Tự động tăng trong DB
    private String auctionId;  // Mã phòng đấu giá
    private String bidderId;   // Mã người đặt (customer_id)
    private double bidAmount;  // Số tiền đặt
    private int bidRank;       // Thứ tự lượt đặt
    private LocalDateTime bidTime; // Thời gian đặt

    // Constructor rỗng
    public BidTransaction() {}

    // Constructor để dùng khi tạo lượt đặt mới
    public BidTransaction(String auctionId, String bidderId, double bidAmount, int bidRank) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidRank = bidRank;
        this.bidTime = LocalDateTime.now(); // Lấy giờ hiện tại
    }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public int getBidRank() { return bidRank; }
    public void setBidRank(int bidRank) { this.bidRank = bidRank; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
}