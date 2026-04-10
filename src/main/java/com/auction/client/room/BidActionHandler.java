package com.auction.client.room;

import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.server.service.AuctionService;

import java.time.LocalDateTime;

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

                if (room.getStartTime() != null && now.isBefore(room.getStartTime())) {
                    presenter.appendChat("⚠️ Phiên đấu giá chưa bắt đầu, chưa thể đặt giá!");
                    return;
                }

                if (room.getActualEndTime() != null && !now.isBefore(room.getActualEndTime())) {
                    presenter.disableBidUi("Phiên đấu giá đã hết giờ.");
                    return;
                }
            }

            double amount = Double.parseDouble(request.amountText());
            auctionService.placeBid(amount);
        } catch (NumberFormatException e) {
            presenter.appendChat("Hệ thống: Vui lòng nhập số tiền hợp lệ!");
        }
    }
}