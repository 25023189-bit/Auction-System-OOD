package com.auction.server.service;

import com.auction.common.dto.Message;

import java.time.LocalDateTime;

/**
 * Lớp AuctionService đóng vai trò là tầng Dịch vụ (Service Layer) của Client.
 * Nhiệm vụ chính: Cung cấp các phương thức giao tiếp rõ ràng cho UI Controller gọi.
 * Thay vì Controller phải tự đóng gói Message và gửi đi, nó chỉ cần gọi hàm ở đây.
 */
public class AuctionService {
    // Đối tượng quản lý kết nối Socket thực tế
    private ClientConnection clientConnection;

    // Lưu trữ thông tin định danh của người dùng hiện tại sau khi đăng nhập thành công
    private String currentUser = "";

    /**
     * Khởi tạo service với một kết nối đã có sẵn.
     */
    public AuctionService(ClientConnection connection) {
        this.clientConnection = connection;
    }

    // --- GETTER / SETTER ---
    public void setCurrentUser(String userid) {
        this.currentUser = userid;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public ClientConnection getClientConnection() {
        return this.clientConnection;
    }

    /**
     * Gửi yêu cầu lấy danh sách toàn bộ các phòng đấu giá đang hoạt động.
     */
    public void getRooms() {
        clientConnection.sendMessage(new Message("GET_ROOMS", currentUser, ""));
    }

    // ==========================================================
    // CÁC NGHIỆP VỤ GỬI YÊU CẦU LÊN SERVER
    // ==========================================================

    /**
     * Gửi yêu cầu Đăng nhập.
     * @param id Tên đăng nhập
     * @param password Mật khẩu
     */
    public void login(String id, String password) {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("LOGIN", id, password));
        }
    }

    public void register(String username, String password, String role) {
        String data = username + "|" + password + "|" + role;
        clientConnection.sendMessage(new Message("REGISTER", "", data));
    }

    /**
     * Gửi yêu cầu tham gia vào một phòng đấu giá cụ thể.
     */
    public void joinRoom(String roomId) {
        clientConnection.sendMessage(new Message("JOIN_ROOM", currentUser, roomId));
    }

    /**
     * Gửi yêu cầu rời khỏi phòng đấu giá hiện tại.
     */
    public void leaveRoom() {
        clientConnection.sendMessage(new Message("LEAVE_ROOM", this.currentUser, ""));
    }

    /**
     * Gửi yêu cầu đặt giá (Bid) cho vật phẩm trong phòng.
     */
    public void placeBid(double amount) {
        clientConnection.sendMessage(new Message("BID", currentUser, amount));
    }

    /**
     * Gửi yêu cầu chốt phiên đấu giá.
     */
    public void closeAuction(String roomId) {
        clientConnection.sendMessage(new Message("CLOSE_AUCTION", currentUser, roomId));
    }

    /**
     * 🌟 ĐÃ SỬA: Gửi tin nhắn chat vào phòng đấu giá.
     */
    public void sendChat(String chatContent) {
        // Dùng this.currentUser thay vì ClientConnection.currentUser
        Message msg = new Message("CHAT_MSG", this.currentUser, chatContent);
        clientConnection.sendMessage(msg);
    }

    /**
     * Gửi yêu cầu lấy lại mật khẩu.
     */
    public void resetPassword(String username, String newPassword){
        clientConnection.sendMessage(new Message("RESET_PASSWORD", username, newPassword));
    }

    /**
     * (Nghiệp vụ Seller) Gửi yêu cầu tạo phòng đấu giá mới.
     */
    public void createAuction(String itemName, String itemDesc, double startingPrice, LocalDateTime startTime, int duration) {
        // 🔥 ĐÃ SỬA: Nối thêm startTime và duration vào chuỗi data phân cách bởi dấu "|"
        String data = itemName + "|" + itemDesc + "|" + startingPrice + "|" + startTime.toString() + "|" + duration;

        // Dùng this.currentUser để đảm bảo đúng người gửi
        clientConnection.sendMessage(new Message("CREATE_AUCTION", this.currentUser, data));
    }

    /**
     * Gửi yêu cầu lấy lịch sử đặt giá của một phòng cụ thể lên Server.
     * @param roomId Mã phòng đấu giá cần xem
     */
    public void getBidHistory(String roomId) {
        Message msg = new Message("GET_BID_HISTORY", currentUser, roomId);
        clientConnection.sendMessage(msg);
    }

    /**
     * SỬ DỤNG TRONG TRƯỜNG HỢP KHẨN CẤP
     */
    public void disconnect() {
        if (clientConnection != null) {
            clientConnection.closeConnection();
        }
        this.currentUser = "";
    }
}