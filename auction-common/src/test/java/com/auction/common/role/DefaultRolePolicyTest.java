package com.auction.common.role;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRolePolicyTest {
    private DefaultRolePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DefaultRolePolicy();
    }

    @Test
    @DisplayName("Test canCreateAuction - Phân quyền tạo phiên đấu giá (Chỉ SELLER)")
    void testCanCreateAuction() {
        // 1. Chặn trường hợp user bị null
        assertFalse(policy.canCreateAuction(null), "User null không được tạo");

        // 2. Chặn role không hợp lệ
        User bidder = new User();
        bidder.setRole("BIDDER");
        assertFalse(policy.canCreateAuction(bidder), "BIDDER không được tạo");

        // 3. Đúng role SELLER
        User seller = new User();
        seller.setRole("SELLER");
        assertTrue(policy.canCreateAuction(seller), "SELLER được tạo");

        // 4. Đúng role nhưng viết chữ thường (Test case-insensitive)
        User sellerLower = new User();
        sellerLower.setRole("seller");
        assertTrue(policy.canCreateAuction(sellerLower), "seller chữ thường vẫn được tạo");
    }

    @Test
    @DisplayName("Test canCloseAuction - Phân quyền đóng phiên đấu giá (Chính chủ)")
    void testCanCloseAuction() {
        User user = new User();
        AuctionRoom room = new AuctionRoom();
        String userId = "USER123";

        // 1. Chặn tất cả các trường hợp truyền thiếu dữ liệu (null)
        assertFalse(policy.canCloseAuction(null, room, userId));
        assertFalse(policy.canCloseAuction(user, null, userId));
        assertFalse(policy.canCloseAuction(user, room, null));

        // 2. Chặn trường hợp phòng chưa có tên người bán
        assertFalse(policy.canCloseAuction(user, room, userId));

        // 3. Chặn trường hợp ID người dùng hiện tại không khớp với ID chủ phòng
        room.setSellerName("USER999");
        assertFalse(policy.canCloseAuction(user, room, userId));

        // 4. Cấp quyền khi ID khớp hoàn toàn (Test luôn khả năng tự động xóa khoảng trắng và không phân biệt hoa/thường)
        room.setSellerName("  user123  ");
        assertTrue(policy.canCloseAuction(user, room, " USER123"));
    }

    @Test
    @DisplayName("Test isAdmin - Phân quyền quản trị viên (Chỉ ADMIN)")
    void testIsAdmin() {
        assertFalse(policy.isAdmin(null));

        User seller = new User();
        seller.setRole("SELLER");
        assertFalse(policy.isAdmin(seller));

        User admin = new User();
        admin.setRole("ADMIN");
        assertTrue(policy.isAdmin(admin));

        User adminLower = new User();
        adminLower.setRole("admin");
        assertTrue(policy.isAdmin(adminLower));
    }
}