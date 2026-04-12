package com.auction.common.model;

/**
 * Lớp đại diện cho Người bán (Seller).
 * Kế thừa toàn bộ thuộc tính và phương thức từ User.
 */
public class Seller extends User {
    private static final long serialVersionUID = 1L;

    private int successfulAuctions;
    private double ratingScore;

    // ✅ Constructor rỗng đã được đưa VÀO TRONG class
    public Seller() {
        super(); // Gọi constructor rỗng của User
    }

    public Seller(String id, String username, String password, double balance) {
        super(id, username, password, balance);
        this.successfulAuctions = 0;
        this.ratingScore = 5.0;
    }

    public Seller(String id, String username, String role, String password, double balance) {
        super(id, username, role, password, balance);
        this.successfulAuctions = 0;
        this.ratingScore = 5.0;
    }

    // Cập nhật điểm uy tín sau mỗi phiên đấu giá (Tính trung bình cộng)
    public void updateRating(int stars) {
        this.ratingScore = ((this.ratingScore * successfulAuctions) + stars) / (successfulAuctions + 1);
        this.successfulAuctions++;
    }

    // Nhận tiền khi có người mua thành công
    public void addBalance(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    // Kiểm tra xem Seller này có bị "banned" vì uy tín quá thấp không
    public boolean isTrustworthy() {
        return this.ratingScore >= 2.0;
    }

    public double getRatingScore() { return ratingScore; }
    public int getSuccessfulAuctions() { return successfulAuctions; }

    @Override
    public String toString() {
        return "Seller{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", ratingScore=" + ratingScore +
                '}';
    }
}