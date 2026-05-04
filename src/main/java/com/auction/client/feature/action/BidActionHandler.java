package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.feature.presenter.AuctionRoomPresenter;

/**
 * Handler legacy xử lý thao tác đặt giá cho package presenter cũ.
 *
 * Vai trò:
 * - Bọc lời gọi AuctionService.placeBid() cho luồng UI cũ.
 * - Giữ dependency SessionStore/Presenter để tương thích constructor hiện có.
 *
 * Luồng chính:
 * 1. UI legacy gọi placeBid(roomId, bidAmount).
 * 2. Handler gửi số tiền bid lên AuctionService nếu service khả dụng.
 *
 * Business rules:
 * - roomId không được dùng trực tiếp ở đây vì service/server dựa vào phòng hiện tại của session.
 * - Validate quyền bid và giá hợp lệ vẫn do server/service phòng xử lý cuối cùng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: giữ service/session/presenter mutable.
 * - Dependency: AuctionService, SessionStore, feature.presenter.AuctionRoomPresenter.
 */
public class BidActionHandler {
    private AuctionService auctionService;
    private SessionStore sessionStore;
    private AuctionRoomPresenter presenter;

    public BidActionHandler(AuctionService auctionService, SessionStore sessionStore, AuctionRoomPresenter presenter) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.presenter = presenter;
    }

    public void placeBid(String roomId, double bidAmount) {
        if (auctionService != null) {
            // AuctionService lấy phòng hiện tại từ session/server nên không cần gửi roomId ở đây.
            auctionService.placeBid(bidAmount);
        }
    }
}
