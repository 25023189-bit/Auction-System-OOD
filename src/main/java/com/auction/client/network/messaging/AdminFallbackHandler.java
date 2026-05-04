package com.auction.client.network.messaging;

import com.auction.client.feature.controllers.AdminController;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;

import java.util.List;

/**
 * Fallback handler chuyển phản hồi admin về AdminController đang mở.
 *
 * Vai trò:
 * - Route danh sách user, auction, pending request và bid history vào bảng admin.
 * - Chuyển kết quả thao tác admin thành thông báo success/fail trên dashboard.
 *
 * Luồng chính:
 * 1. ResponseRouter/FallbackMessageHandler chuyển các action ADMIN_* hoặc BID_HISTORY_SUCCESS vào handler.
 * 2. Handler lấy AdminController từ SessionStore và gọi method cập nhật tương ứng.
 *
 * Business rules:
 * - Nếu dashboard admin chưa mở thì bỏ qua response admin.
 * - ADMIN_ACTION_SUCCESS cần giữ thêm mã con trong msg.id để controller reload bảng phù hợp.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: phụ thuộc AdminController/TableView JavaFX, controller tự dùng Platform.runLater khi cần.
 * - Dependency: MessageHandler, SessionStore, AdminController, User, AuctionRoom, BidTransaction, PendingAuctionRequest.
 */
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
                 "ADMIN_PENDING_AUCTION_LIST",
                 "BID_HISTORY_SUCCESS",
                 "ADMIN_ACTION_SUCCESS",
                 "ADMIN_ACTION_FAIL" -> true;
            default -> false;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Message msg) {
        // Nếu dashboard admin chưa mở thì không có bảng nào cần cập nhật.
        AdminController admin = sessionStore.getAdminController();
        if (admin == null) return;

        switch (msg.getAction()) {
            case "ADMIN_USER_LIST" -> admin.updateUsersTable((List<User>) msg.getData());

            case "ADMIN_AUCTION_LIST" -> admin.updateAuctionsTable((List<AuctionRoom>) msg.getData());

            case "ADMIN_PENDING_AUCTION_LIST" ->
                    admin.updatePendingAuctionsTable((List<PendingAuctionRequest>) msg.getData());

            case "BID_HISTORY_SUCCESS" -> admin.updateBidHistoryTable((List<BidTransaction>) msg.getData());

            case "ADMIN_ACTION_SUCCESS" ->
                    admin.handleAdminResponse(msg.getAction() + "_" + msg.id, String.valueOf(msg.getData()));

            case "ADMIN_ACTION_FAIL" -> admin.handleAdminResponse("FAIL", String.valueOf(msg.getData()));
        }
    }
}
