package com.auction.server.service;

import com.auction.common.dto.Message;

import java.time.LocalDateTime;

public class AuctionService {
    private ClientConnection clientConnection;
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

    public void resetPassword(String username, String newPassword) {
        clientConnection.sendMessage(new Message("RESET_PASSWORD", username, newPassword));
    }

    public void forgotPassword(String username) {
        clientConnection.sendMessage(new Message("FORGOT_PASSWORD", username, ""));
    }

    public void createAuction(String itemName, String itemDesc, double startingPrice,
                              double minimumJoinAmount, double bidStep,
                              LocalDateTime startTime, int duration, int extensionSeconds) {
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
    // PHẦN XỬ LÝ LẤY CHI TIẾT SẢN PHẨM (PRODUCT VIEW)
    // ==========================================================
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
        // Tạm thời in ra log. Khi làm tính năng Socket, ta sẽ gửi message này qua Server
        System.out.println("[Chat - Room " + roomId + "]: " + message);
    }
}
