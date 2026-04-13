package com.auction.client.feature.room;

import com.auction.common.role.RolePolicy;
import com.auction.client.session.SessionStore;
import com.auction.client.core.ui.ViewStateBinder;
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

        // Chỗ này nó gọi room.getItemDescription(), nên thằng AuctionRoom phải có hàm này
        presenter.showRoomInfo(room.getItemName(), room.getCurrentPrice(), room.getItemDescription());

        if (room.isEntryLocked()) {
            presenter.appendChat("🔒 Phiên đã khóa người tham gia mới (30 giây cuối).");
        }

        User user = sessionStore.getCurrentUser();
        String username = sessionStore.getCurrentUsername();
        boolean isOwner = rolePolicy.canCloseAuction(user, room, username);
        presenter.setOwnerControlsVisible(isOwner);
    }
}