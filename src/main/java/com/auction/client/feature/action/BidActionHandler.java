package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.feature.presenter.AuctionRoomPresenter;

/**
 * Handles bid placement actions
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
            auctionService.placeBid(roomId, bidAmount);
        }
    }
}
