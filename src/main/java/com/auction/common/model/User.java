package com.auction.common.model;

import java.io.Serializable;

/**
 * Lớp User đại diện cho thực thể người dùng trong hệ thống.
 * Tuân thủ quy tắc đóng gói (Encapsulation) và Serializable để truyền qua Socket.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;        // Định dạng BD5xxxxx
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;      // BIDDER hoặc SELLER
    private String status;    // ACTIVE, LOCKED...

    // Constructor mặc định (Cần thiết cho một số thư viện mapping)
    public User() {
    }

    // Constructor rút gọn cho Login/Register
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * BỔ SUNG: Constructor đầy đủ
     * Dùng khi lấy dữ liệu từ Database (UserDAO) lên Object
     */
    public User(String id, String username, String password, String fullName, String email, String role, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
}