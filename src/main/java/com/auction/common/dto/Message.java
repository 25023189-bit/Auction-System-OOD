package com.auction.common.dto;

import java.io.Serializable;

/**
 * Lớp Message: "Ngôn ngữ chung" để Client và Server giao tiếp.
 * Phải implements Serializable để truyền qua Socket.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;   // Ví dụ: LOGIN, BID, CHAT_MSG...
    private String id;       // Ví dụ: RoomID hoặc UserID
    private String username;
    private String role;
    private Object data;     // Payload (có thể là String, Double, hoặc AuctionRoom object)

    // 1. Constructor đầy đủ nhất
    public Message(String action, String id, String username, String role, Object data) {
        this.action = action;
        this.id = id;
        this.username = username;
        this.role = role;
        this.data = data;
    }

    // 2. Constructor phổ biến (Dùng cho Request từ Client: Action - ID - Data)
    public Message(String action, String id, Object data) {
        this.action = action;
        this.id = id;
        this.data = data;
    }

    // 3. Constructor phản hồi (Dùng cho Response từ Server: Action - Data - Role)
    // Đã đảo vị trí Object lên trước để Java phân biệt được với Constructor số 2
    public Message(String action, Object data, String role) {
        this.action = action;
        this.data = data;
        this.role = role;
    }

    // --- GETTERS ---
    public String getAction() { return action; }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Object getData() { return data; }

    // --- SETTERS ---
    public void setAction(String action) { this.action = action; }
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setData(Object data) { this.data = data; }
}