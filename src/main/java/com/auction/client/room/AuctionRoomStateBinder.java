package com.auction.client.room;

import com.auction.client.role.RolePolicy;
import com.auction.client.session.SessionStore;
import com.auction.client.ui.ViewStateBinder;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

public class AuctionRoomStateBinder implements ViewStateBinder<AuctionRoom> {
    private final AuctionRoomPresenter presenter;
    private final SessionStore sessionStore;
    private final RolePolicy rolePolicy;

    public AuctionRoomStateBinder(AuctionRoomPresenter presenter,
                                  SessionStore sessionStore,
                                  RolePolicy rolePolicy) {
        this.presenter = presenter;
        this.sessionStore = sessionStore;
        this.rolePolicy = rolePolicy;
    }

    @Override
    public void bind(AuctionRoom room) {
        if (room == null) return;

        presenter.showRoomInfo(room.getItemName(), room.getCurrentPrice(), room.getItemDescription());

        User user = sessionStore.getCurrentUser();
        String username = sessionStore.getCurrentUsername();
        boolean isOwner = rolePolicy.canCloseAuction(user, room, username);
        presenter.setOwnerControlsVisible(isOwner);
    }
}