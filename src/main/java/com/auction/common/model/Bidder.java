package com.auction.common.model;

/**
 * Lớp đại diện cho Người mua (Bidder).
 * Kế thừa toàn bộ thuộc tính và phương thức từ User.
 */
public class Bidder extends User {

    // ✅ ĐÃ SỬA: Đưa Constructor rỗng vào BÊN TRONG class
    public Bidder() {
        super(); // Gọi constructor rỗng của User
    }

    public Bidder(String id, String username, String role, String password, double balance) {
        super(id, username, role, password, balance);
    }

    public Bidder(String id, String username, String password, double balance) {
        super(id, username, password, balance);
    }

    // Nạp tiền
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    // Kiểm tra xem có đủ tiền để ra giá (Bid) khoản này không?
    public boolean canAfford(double bidAmount) {
        return this.balance >= bidAmount;
    }

    // Trừ tiền khi thắng đấu giá
    public boolean deduct(double amount) {
        if (canAfford(amount)) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    // Bổ sung toString để dễ debug trên Server
    @Override
    public String toString() {
        return "Bidder{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}