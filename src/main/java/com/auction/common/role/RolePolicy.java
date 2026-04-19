package com.auction.common.role;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

public interface RolePolicy {
    boolean canCreateAuction(User user);
    boolean canCloseAuction(User user, AuctionRoom room, String currentUsername);
    boolean isAdmin(User user);
}