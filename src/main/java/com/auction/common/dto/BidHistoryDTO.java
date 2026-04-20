package com.auction.common.dto;

import java.time.LocalDateTime;

public class BidHistoryDTO {
    private String bidderName;   // Tên người đặt để hiển thị lên UI
    private double amount;       // Số tiền đặt
    private String time; // Thời điểm đặt giá

    // Constructor mặc định
    public BidHistoryDTO() {}

    // Constructor có tham số để khởi tạo nhanh
    public BidHistoryDTO(String bidderName, double amount, String time) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.time = time;
    }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}