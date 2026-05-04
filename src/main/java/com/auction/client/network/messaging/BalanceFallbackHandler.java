package com.auction.client.network.messaging;

import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.service.AuctionService;

/**
 * Fallback handler cập nhật số dư ví khi server gửi UPDATE_BALANCE.
 *
 * Vai trò:
 * - Lọc message số dư đúng với user đang đăng nhập trên client này.
 * - Cập nhật User trong SessionStore và bind lại header lobby nếu có.
 *
 * Luồng chính:
 * 1. FallbackMessageHandler chuyển UPDATE_BALANCE vào handler.
 * 2. Handler kiểm tra msg.id, đọc balance mới, cập nhật session user và LobbyUserInfoBinder.
 *
 * Business rules:
 * - Bỏ qua message không dành cho currentUser của AuctionService.
 * - Bỏ qua payload không phải Double để tránh cập nhật sai số dư.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: SessionStore/User và binder UI mutable, gọi trên luồng UI.
 * - Dependency: MessageHandler, AuctionService, SessionStore, LobbyUserInfoBinder, User.
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
