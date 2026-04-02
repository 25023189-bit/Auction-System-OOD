package com.auction.common.model;

import java.io.Serializable;

public class User implements Serializable, Entity {
    // Thuộc tính private để đóng gói (Encapsulation)
    protected String username;
    protected String password;
    protected String id;
    protected String role;
    protected double balance;

    // Constructor (Hàm khởi tạo)

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
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

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return username;
    }

    public String getUsername(){return username;}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}