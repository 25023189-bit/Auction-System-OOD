package com.auction.server.service; // Lưu ý: Dù nằm ở package server nhưng class này phục vụ Client

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

    public void login(String id, String password) {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("LOGIN", id, password));
        }
    }

    public void register(String username, String password, String role) {
        String data = username + "|" + password + "|" + role;
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

    public void createAuction(String itemName, String itemDesc, double startingPrice,
                              LocalDateTime startTime, int duration, int extensionSeconds) {
        String data = itemName + "|" + itemDesc + "|" + startingPrice + "|" +
                startTime.toString() + "|" + duration + "|" + extensionSeconds;
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
}