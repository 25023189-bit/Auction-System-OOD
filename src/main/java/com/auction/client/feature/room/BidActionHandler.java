package com.auction.client.feature.room;

import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.server.service.AuctionService;

import java.time.LocalDateTime;

/**
 * ActionHandler xử lý thao tác đặt giá từ UI phòng đấu giá.
 *
 * Vai trò:
 * - Parse số tiền bid từ TextField request.
 * - Kiểm tra nhanh trạng thái thời gian phía client trước khi gọi AuctionService.placeBid().
 *
 * Luồng chính:
 * 1. AuctionController tạo BidRequest và gọi handle().
 * 2. Handler đọc room hiện tại, kiểm tra start/end time, parse amount và gửi bid lên server.
 *
 * Business rules:
 * - Không cho bid trước startTime hoặc sau scheduledEndTime trên UI.
 * - NumberFormatException phải được chuyển thành thông báo thân thiện trong chat log.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: dùng SessionStore/Presenter mutable trên luồng UI.
 * - Dependency: ActionHandler<BidRequest>, AuctionService, SessionStore, AuctionRoomPresenter, AuctionRoom.
 */
public class BidActionHandler implements ActionHandler<BidRequest> {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;
    private final AuctionRoomPresenter presenter;

    public BidActionHandler(AuctionService auctionService,
                            SessionStore sessionStore,
                            AuctionRoomPresenter presenter) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.presenter = presenter;
    }

    @Override
    public void handle(BidRequest request) {
        try {
            AuctionRoom room = sessionStore.getCurrentRoom();
            if (room != null) {
                LocalDateTime now = LocalDateTime.now();

                // Client chặn bid ngoài thời gian hợp lệ; server vẫn là nơi xác thực cuối cùng.
                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    presenter.appendChat("Auction has not started yet. Bidding is not available.");
                    return;
                }

                if (room.getScheduledEndTime() != null && !now.isBefore(room.getScheduledEndTime())) {
                    presenter.disableBidUi("Auction has ended.");
                    return;
                }
            }

            // Parse số tiền từ TextField trước khi gọi AuctionService.
            double amount = Double.parseDouble(request.amountText());
            auctionService.placeBid(amount);
        } catch (NumberFormatException e) {
            presenter.appendChat("System: Please enter a valid amount.");
        }
    }
}
