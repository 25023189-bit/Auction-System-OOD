package com.auction.client.feature.room;

import com.auction.client.core.ui.ViewStateBinder;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.common.role.RolePolicy;

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
        if (room == null) {
            return;
        }

        presenter.showRoomInfo(room.getItemName(), room.getCurrentPrice(), room.getItemDescription());

        if (room.isEntryLocked()) {
            presenter.appendChat("Phiên đã khóa người tham gia mới (30 giây cuối).");
        }

        User user = sessionStore.getCurrentUser();
        String currentUserId = user != null ? user.getId() : null;
        boolean isOwner = rolePolicy.canCloseAuction(user, room, currentUserId);
        presenter.setOwnerControlsVisible(isOwner);
    }
}
