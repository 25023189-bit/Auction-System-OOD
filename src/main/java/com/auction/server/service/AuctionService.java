package com.auction.server.service;

import com.auction.common.dto.Message;

import java.time.LocalDateTime;

/**
 * Service phía client dùng để đóng gói Message gửi tới server.
 * Dù nằm trong package server.service, lớp này đang đóng vai trò client-side API cho JavaFX controller.
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

    public void register(String customerId, String username, String email, String fullName,
                         String password, String role, String organization) {
        // Dùng dấu | làm protocol đơn giản giữa client và AuthActionHandler.
        String normalizedOrganization = (organization == null || organization.trim().isEmpty()) ? "" : organization.trim();
        String data = customerId + "|" + username + "|" + email + "|" + fullName + "|" + password + "|" + role + "|" + normalizedOrganization;
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

    public void resetPassword(String username, String newPassword){
        clientConnection.sendMessage(new Message("RESET_PASSWORD", username, newPassword));
    }

    public void forgotPassword(String username) {
        clientConnection.sendMessage(new Message("FORGOT_PASSWORD", username, ""));
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
    public void sendChatMessage(String roomId, String message) {
        // Hỗ trợ package action cũ: hiện chỉ log local, luồng chat mới dùng sendChat().
        System.out.println("[Chat - Room " + roomId + "]: " + message);
    }
}
