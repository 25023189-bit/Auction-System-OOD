package com.auction.client.feature.controllers.account.admin.action;

import com.auction.client.service.AuctionService;
import com.auction.common.dto.Message;

public class AdminActionSender {
    private final AuctionService auctionService;

    public AdminActionSender(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void loadUsers() {
        send("ADMIN_GET_USERS", "");
    }

    public void loadAuctions() {
        send("ADMIN_GET_AUCTIONS", "");
    }

    public void loadPendingAuctions() {
        send("ADMIN_GET_PENDING_AUCTIONS", "");
    }

    public void deleteUser(String userId) {
        send("ADMIN_DELETE_USER", userId);
    }

    public void deleteAuction(String roomId) {
        send("ADMIN_DELETE_AUCTION", roomId);
    }

    public void approveAuction(String requestId) {
        send("ADMIN_APPROVE_AUCTION", requestId);
    }

    public void rejectAuction(String requestId) {
        send("ADMIN_REJECT_AUCTION", requestId);
    }

    private void send(String action, Object data) {
        auctionService.getClientConnection().sendMessage(new Message(action, auctionService.getCurrentUser(), data));
    }
}
