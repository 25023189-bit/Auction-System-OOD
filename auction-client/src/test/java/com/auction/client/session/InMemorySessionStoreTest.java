package com.auction.client.session;

import com.auction.client.feature.controllers.account.admin.AdminController;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class InMemorySessionStoreTest {
    private InMemorySessionStore store;

    @BeforeEach
    void setUp() {
        store = new InMemorySessionStore();
    }

    @Test
    @DisplayName("Test getter and setter cho RoomId")
    void testRoomId() {
        store.setCurrentRoomId("R1");
        assertEquals("R1", store.getCurrentRoomId());
    }

    @Test
    @DisplayName("Test getter and setter cho Room")
    void testRoom() {
        AuctionRoom room = new AuctionRoom();
        store.setCurrentRoom(room);
        assertEquals(room, store.getCurrentRoom());
    }

    @Test
    @DisplayName("Test getter and setter cho User")
    void testUser() {
        User user = new User();
        store.setCurrentUser(user);
        assertEquals(user, store.getCurrentUser());
    }

    @Test
    @DisplayName("Test getter and setter cho Username")
    void testUsername() {
        store.setCurrentUsername("hanto");
        assertEquals("hanto", store.getCurrentUsername());
        store.setCurrentUsername(null); // Test fallback
        assertEquals("", store.getCurrentUsername());
    }

    @Test
    @DisplayName("Test getter and setter cho AdminController")
    void testAdminController() {
        AdminController admin = mock(AdminController.class);
        store.setAdminController(admin);
        assertEquals(admin, store.getAdminController());
    }

    @Test
    @DisplayName("Test clearSession xóa sạch toàn bộ dữ liệu")
    void testClearSession() {
        store.setCurrentRoomId("R1");
        store.setCurrentUsername("han");
        store.setCurrentUser(new User());
        store.setCurrentRoom(new AuctionRoom());
        store.setAdminController(mock(AdminController.class));

        store.clearSession();

        assertNull(store.getCurrentRoomId());
        assertNull(store.getCurrentRoom());
        assertNull(store.getCurrentUser());
        assertEquals("", store.getCurrentUsername());
        assertNull(store.getAdminController());
    }
}
