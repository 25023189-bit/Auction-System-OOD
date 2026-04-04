package com.auction.common.model;

import java.io.Serializable;

public class User implements Serializable, Entity {
    private static final long serialVersionUID = 1L;
    // Thuộc tính private để đóng gói (Encapsulation)
    protected String username;
    protected String password;
    protected String id;
    protected String role;
    protected double balance = 1000;
    private String fullName;
    private String email;
    private String status;    // ACTIVE, LOCKED...

    // Constructor (Hàm khởi tạo)

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor rút gọn cho Login/Register
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User(String id, String username,String role, String password) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.password = password;
    }

    public User(String id, String username, String password,double balance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public User(String id, String username,String role, String password, double balance) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.password = password;
        this.balance = balance;
    }

    public boolean isPasswordStrong() {
        if (this.password == null) return false;
        if (this.password.length() >= 6 && !this.password.contains(" ")){
            return true;
        }else {return false;}
    }

    public User(String id, String username, String password, String fullName, String email, String role, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getName() {
        return username;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public double getBalance() { return balance; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setBalance(double balance) { this.balance = balance; }
}