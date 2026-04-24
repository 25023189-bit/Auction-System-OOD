package com.auction.common.model;

import com.auction.common.dto.BidHistoryDTO;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class ProductDetailResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Thông tin cơ bản
    private String title;
    private String description;
    private String author; // Dành riêng cho vật phẩm là Art

    // Thông tin giá và thời gian
    private double startPrice;
    private double currentPrice;
    private long timeLeftMillis;

    // Lịch sử đặt giá
    private List<BidHistoryDTO> bidHistory;

    // Constructor rỗng (Bắt buộc phải có để truyền qua mạng)
    public ProductDetailResponse() {
    }

    // ==========================================
    // TẤT CẢ CÁC HÀM GETTER / SETTER Ở DƯỚI ĐÂY
    // ==========================================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public long getTimeLeftMillis() {
        return timeLeftMillis;
    }

    public void setTimeLeftMillis(long timeLeftMillis) {
        this.timeLeftMillis = timeLeftMillis;
    }

    public List<BidHistoryDTO> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<BidHistoryDTO> bidHistory) {
        this.bidHistory = bidHistory;
    }
}
