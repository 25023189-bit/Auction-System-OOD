package com.auction.client.service;

import com.auction.common.dto.Message;
import com.auction.client.network.socket.ClientConnection;

import java.time.LocalDateTime;

/**
 * API phía client để đóng gói Message gửi tới AuctionServer.
 *
 * Vai trò:
 * - Cung cấp các hàm tiện ích cho JavaFX controller gọi login, register, join, bid, chat và tạo auction.
 * - Giữ currentUser và chuyển tham số UI thành payload theo protocol Message.
 *
 * Luồng chính:
 * 1. Controller gọi method tương ứng với hành động người dùng.
 * 2. AuctionService tạo Message đúng action/id/data rồi gửi qua ClientConnection.
 *
 * Business rules:
 * - Các action cần xác thực phải gửi kèm currentUser sau khi login thành công.
 * - Payload register/createAuction dùng dấu |, nên các bên gửi/nhận phải thống nhất thứ tự field.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: currentUser và callback product detail là state mutable phía client.
 * - Dependency: ClientConnection, Message, LocalDateTime, JavaFX controller callback.
 */
public class AuctionService {
    private ClientConnection clientConnection;
    // currentUser là customer_id của user đã đăng nhập, được gửi kèm các action cần xác thực.
    private String currentUser = "";

    public AuctionService(ClientConnection connection) {
        this.clientConnection = connection;
    }

    public void setCurrentUser(String userid) {
        this.currentUser = userid;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public ClientConnection getClientConnection() {
        return this.clientConnection;
    }

    public void getRooms() {
        clientConnection.sendMessage(new Message("GET_ROOMS", currentUser, ""));
    }

    public void login(String username, String password) {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("LOGIN", username, password));
        }
    }

    public void register(String customerId, String username, String fullName,
                         String password, String role, String organization) {
        // Dùng dấu | làm protocol đơn giản giữa client và AuthActionHandler.
        String normalizedOrganization = (organization == null || organization.trim().isEmpty()) ? "" : organization.trim();
        String data = customerId + "|" + username + "|" + fullName + "|" + password + "|" + role + "|" + normalizedOrganization;
        clientConnection.sendMessage(new Message("REGISTER", "", data));
    }

    public void joinRoom(String auctionId) {
        clientConnection.sendMessage(new Message("JOIN_ROOM", currentUser, auctionId));
    }

    public void leaveRoom() {
        clientConnection.sendMessage(new Message("LEAVE_ROOM", this.currentUser, ""));
    }

    public void placeBid(double amount) {
        clientConnection.sendMessage(new Message("BID", currentUser, amount));
    }

    public void closeAuction(String auctionId) {
        clientConnection.sendMessage(new Message("CLOSE_AUCTION", currentUser, auctionId));
    }

    public void sendChat(String chatContent) {
        Message msg = new Message("CHAT_MSG", this.currentUser, chatContent);
        clientConnection.sendMessage(msg);
    }

    public void resetPassword(String username, String newPassword, String confirmPassword) {
        clientConnection.sendMessage(new Message("RESET_PASSWORD", username, newPassword + ":" + confirmPassword));
    }

    public void createAuction(String itemName, String itemDesc, double startingPrice,
                              double minimumJoinAmount, double bidStep,
                              LocalDateTime startTime, int duration, int extensionSeconds) {
        // SellerActionHandler sẽ parse lại chuỗi này và validate ở server.
        String data = itemName + "|" + itemDesc + "|" + startingPrice + "|" +
                minimumJoinAmount + "|" + bidStep + "|" +
                startTime + "|" + duration + "|" + extensionSeconds;
        clientConnection.sendMessage(new Message("CREATE_AUCTION", this.currentUser, data));
    }

    public void getBidHistory(String auctionId) {
        Message msg = new Message("GET_BID_HISTORY", currentUser, auctionId);
        clientConnection.sendMessage(msg);
    }

    public void disconnect() {
        if (clientConnection != null) {
            clientConnection.closeConnection();
        }
        this.currentUser = "";
    }

    // ==========================================================
    // PRODUCT DETAIL CALLBACK
    // ==========================================================
    // Callback cho popup product detail khi server trả PRODUCT_DETAILS_SUCCESS.
    private java.util.function.Consumer<Object> productDetailsCallback;

    public void setProductDetailsCallback(java.util.function.Consumer<Object> callback) {
        this.productDetailsCallback = callback;
    }

    public void requestProductDetails(String roomId) {
        if (clientConnection != null) {
            com.auction.common.dto.Message msg = new com.auction.common.dto.Message("GET_PRODUCT_DETAILS", "CLIENT", roomId);
            clientConnection.sendMessage(msg);
        }
    }

    public void fireProductDetailsReceived(Object data) {
        if (productDetailsCallback != null) {
            productDetailsCallback.accept(data);
        }
    }

}
