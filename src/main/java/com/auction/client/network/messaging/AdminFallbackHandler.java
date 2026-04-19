package com.auction.client.network.messaging;

import com.auction.client.feature.controllers.AdminController;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.User;

import java.util.List;

public class AdminFallbackHandler implements MessageHandler {
    private final SessionStore sessionStore;

    public AdminFallbackHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public boolean supports(String action) {
        return switch (action) {
            case "ADMIN_USER_LIST",
                 "ADMIN_AUCTION_LIST",
                 "BID_HISTORY_SUCCESS",
                 "ADMIN_ACTION_SUCCESS",
                 "ADMIN_ACTION_FAIL" -> true;
            default -> false;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Message msg) {
        AdminController admin = sessionStore.getAdminController();
        if (admin == null) return;

        switch (msg.getAction()) {
            case "ADMIN_USER_LIST" ->
                    admin.updateUsersTable((List<User>) msg.getData());

            case "ADMIN_AUCTION_LIST" ->
                    admin.updateAuctionsTable((List<AuctionRoom>) msg.getData());

            case "BID_HISTORY_SUCCESS" ->
                    admin.updateBidHistoryTable((List<BidTransaction>) msg.getData());

            case "ADMIN_ACTION_SUCCESS" ->
                    admin.handleAdminResponse(msg.getAction() + "_" + msg.id, String.valueOf(msg.getData()));

            case "ADMIN_ACTION_FAIL" ->
                    admin.handleAdminResponse("FAIL", String.valueOf(msg.getData()));
        }
    }
}