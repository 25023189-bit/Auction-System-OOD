package com.auction.common.dto;

import java.io.Serial;
import java.io.Serializable;

public class AutoBidRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomId;    // Để Server biết Robot này hoạt động ở phòng nào
    private double maxBid;    // Giá trần
    private double increment; // Bước giá tự động nhảy

    public AutoBidRequest(String roomId, double maxBid, double increment) {
        this.roomId = roomId;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    // Các hàm Getter thiết yếu cho Server đọc dữ liệu
    public String getRoomId() {
        return roomId;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }
}