package com.auction.client.feature.room;

import com.auction.client.core.ui.ViewStateBinder;
import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.common.role.RolePolicy;

/**
 * Binder đưa dữ liệu AuctionRoom lên UI phòng đấu giá.
 *
 * Vai trò:
 * - Cập nhật thông tin room, participant và trạng thái khóa vào presenter.
 * - Kiểm tra quyền owner để bật/tắt control dành cho seller.
 *
 * Luồng chính:
 * 1. AuctionRoomMessageHandler hoặc controller gọi bind(room) khi có state mới.
 * 2. Binder đọc current user từ session, dùng RolePolicy quyết định quyền đóng phiên và cập nhật presenter.
 *
 * Business rules:
 * - Room null được bỏ qua an toàn.
 * - Quyền đóng phiên phụ thuộc cả role hiện tại và seller sở hữu phòng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: presenter/session JavaFX mutable, không có synchronization.
 * - Dependency: ViewStateBinder<AuctionRoom>, AuctionRoomPresenter, SessionStore, RolePolicy.
 */
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
        presenter.showParticipantCount(room.getParticipantCount());

        if (room.isEntryLocked()) {
            presenter.appendChat("New participants are locked during the final 30 seconds.");
        }

        // Quyền đóng phiên phụ thuộc cả role hiện tại và seller sở hữu phòng.
        User user = sessionStore.getCurrentUser();
        String currentUserId = user != null ? user.getId() : null;
        boolean isOwner = rolePolicy.canCloseAuction(user, room, currentUserId);
        presenter.setOwnerControlsVisible(isOwner);
    }
}
