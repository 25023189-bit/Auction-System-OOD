package com.auction.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidTransaction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int transactionId; // Tự động tăng trong DB
    private String auctionId;  // Mã phòng đấu giá
    private String bidderId;   // Mã người đặt (customer_id)
    private double bidAmount;  // Số tiền đặt
    private int bidRank;       // Thứ tự lượt đặt
    private String bidTime; // Thời gian đặt

    // Constructor rỗng
    public BidTransaction() {
    }

    // Constructor để dùng khi tạo lượt đặt mới
    public BidTransaction(String auctionId, String bidderId, double bidAmount, int bidRank) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidRank = bidRank;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.bidTime = dtf.format(LocalDateTime.now());
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public int getBidRank() {
        return bidRank;
    }

    public void setBidRank(int bidRank) {
        this.bidRank = bidRank;
    }

    public String getBidTime() {
        return bidTime;
    }

    public void setBidTime(String bidTime) {
        this.bidTime = bidTime;
    }
    // Thêm thuộc tính đánh dấu giá cao nhất
    private boolean isHighest;

    // Getter và Setter cho transactionId
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    // Getter và Setter cho isHighest
    public boolean isHighest() {
        return isHighest;
    }

    public void setHighest(boolean highest) {
        isHighest = highest;
    }
}
