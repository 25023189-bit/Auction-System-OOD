package com.auction.common.role;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

public class DefaultRolePolicy implements RolePolicy {

    @Override
    public boolean canCreateAuction(User user) {
        return user != null && "SELLER".equalsIgnoreCase(user.getRole());
    }

    @Override
    public boolean canCloseAuction(User user, AuctionRoom room, String currentUsername) {
        if (user == null || room == null || currentUsername == null) return false;
        return room.getNameSeller() != null
                && room.getNameSeller().trim().equalsIgnoreCase(currentUsername.trim());
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}