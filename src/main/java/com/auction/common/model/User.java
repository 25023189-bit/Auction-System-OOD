package com.auction.common.model;

import java.io.Serializable;

/**
 * Class User chuẩn chỉnh để fix toàn bộ lỗi build.
 */
public class User implements Serializable, Entity {
    private static final long serialVersionUID = 1L;

    protected String customerId;
    protected String username;
    protected String password;
    protected String role;
    protected double balance;

    // 1. Constructor rỗng (Bắt buộc cho Java Serializable)
    public User() {}

    // 2. Constructor 3 tham số (Cứu bồ cho Admin.java)
    public User(String customerId, String password, String role) {
        this.customerId = customerId;
        this.username = customerId; // Mặc định lấy ID làm tên
        this.password = password;
        this.role = role;
        this.balance = 0.0;
    }

    // 3. Constructor 5 tham số (Dùng cho UserDAO và login)
    public User(String customerId, String username, String role, String password, double balance) {
        this.customerId = customerId;
        this.username = username;
        this.role = role;
        this.password = password;
        this.balance = balance;
    }

    // --- BẮT BUỘC: Override các hàm từ Interface Entity ---

    @Override
    public String getId() {
        return this.customerId;
    }

    @Override
    public String getName() {
        return (this.username != null && !this.username.isEmpty()) ? this.username : this.customerId;
    }

    // --- CÁC HÀM GETTER ĐỂ FIX LỖI "CANNOT FIND SYMBOL" ---

    public String getUsername() {
        return (username != null) ? username : customerId;
    }

    // BỔ SUNG: Alias để fix lỗi ở RegisterFormValidator đòi getConfirmPassword
    public String getConfirmPassword() {
        return this.password;
    }

    public String getCustomerId() { return customerId; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }

    // --- CÁC HÀM SETTER ---

    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setBalance(double balance) { this.balance = balance; }
}