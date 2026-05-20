package com.auction.client.feature.room;

import com.auction.client.session.SessionStore;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.common.role.RolePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuctionRoomStateBinderTest {
    private AuctionRoomPresenter mockPresenter;
    private SessionStore mockSession;
    private RolePolicy mockPolicy;
    private AuctionRoomStateBinder binder;

    @BeforeEach
    void setUp() {
        mockPresenter = mock(AuctionRoomPresenter.class);
        mockSession = mock(SessionStore.class);
        mockPolicy = mock(RolePolicy.class);
        binder = new AuctionRoomStateBinder(mockPresenter, mockSession, mockPolicy);
    }

    @Test
    @DisplayName("Test bind() thoát an toàn khi dữ liệu phòng bị null")
    void testBind_NullRoom() {
        binder.bind(null);
        verifyNoInteractions(mockPresenter, mockSession, mockPolicy);
    }

    @Test
    @DisplayName("Test bind() hiển thị đầy đủ thông tin phòng và cảnh báo khóa phòng (EntryLocked)")
    void testBind_SuccessWithLock() {
        AuctionRoom room = new AuctionRoom();
        room.setItemName("Laptop UET");
        room.setCurrentPrice(1500.0);
        room.setItemDescription("Hàng mới nguyên seal");
        room.setParticipantCount(10);
        room.setEntryLocked(true); // Kích hoạt nhánh cảnh báo 30s cuối

        User user = new User();
        user.setCustomerId("USER_01");

        when(mockSession.getCurrentUser()).thenReturn(user);
        when(mockPolicy.canCloseAuction(user, room, "USER_01")).thenReturn(true);

        binder.bind(room);

        // Xác minh gọi đủ các hàm cập nhật UI
        verify(mockPresenter).showRoomInfo("Laptop UET", 1500.0, "Hàng mới nguyên seal");
        verify(mockPresenter).showParticipantCount(10);
        verify(mockPresenter).appendChat("New participants are locked during the final 30 seconds.");
        verify(mockPresenter).setOwnerControlsVisible(true);
    }
}