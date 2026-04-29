package com.auction.client.session;

import com.auction.client.feature.controllers.AdminController;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

/**
 * Hợp đồng lưu trạng thái phiên hiện tại của client.
 */
public interface SessionStore {
    String getCurrentRoomId();

    void setCurrentRoomId(String roomId);

    AuctionRoom getCurrentRoom();

    void setCurrentRoom(AuctionRoom room);

    User getCurrentUser();

    void setCurrentUser(User user);

    String getCurrentUsername();

    void setCurrentUsername(String username);

    AdminController getAdminController();

    void setAdminController(AdminController adminController);

    void clearSession();
}
