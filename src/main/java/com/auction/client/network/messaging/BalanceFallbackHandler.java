package com.auction.client.network.messaging;

import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.service.AuctionService;

/**
 * Cập nhật số dư ví của user hiện tại khi server gửi UPDATE_BALANCE.
 */
public class BalanceFallbackHandler implements MessageHandler {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;
    private final LobbyUserInfoBinder lobbyUserInfoBinder;

    public BalanceFallbackHandler(AuctionService auctionService,
                                  SessionStore sessionStore,
                                  LobbyUserInfoBinder lobbyUserInfoBinder) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.lobbyUserInfoBinder = lobbyUserInfoBinder;
    }

    @Override
    public boolean supports(String action) {
        return "UPDATE_BALANCE".equals(action);
    }

    @Override
    public void handle(Message msg) {
        // Bỏ qua nếu message không dành cho user đang đăng nhập trên client này.
        if (!String.valueOf(msg.id).equals(auctionService.getCurrentUser())) {
            return;
        }

        if (!(msg.getData() instanceof Double newBalance)) {
            return;
        }

        // Cập nhật model session trước, sau đó bind lại header lobby.
        User user = sessionStore.getCurrentUser();
        if (user != null) {
            user.setBalance(newBalance);
        }

        if (lobbyUserInfoBinder != null) {
            lobbyUserInfoBinder.bind(user);
        }
    }
}
