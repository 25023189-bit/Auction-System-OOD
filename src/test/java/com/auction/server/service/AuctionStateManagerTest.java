package com.auction.server.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionStateManagerTest {

    // Dùng chung một ID phòng giả định cho các bài test
    private static final String TEST_ROOM_ID = "ROOM_VIP_001";

    @AfterEach
    void tearDown() {
        // Dọn dẹp sạch sẽ RAM sau mỗi bài test để đảm bảo các test case không bị ảnh hưởng lẫn nhau
        AuctionStateManager.removeState(TEST_ROOM_ID);
        AuctionStateManager.removeState("GHOST_ROOM");
    }

    @Test
    @DisplayName("getState: Gọi lần đầu tiên phải tạo và trả về một State mới tinh")
    void getState_NewRoom_ReturnsNewInstance() {
        AuctionRuntimeState state = AuctionStateManager.getState(TEST_ROOM_ID);

        assertNotNull(state, "State trả về không được null");
        assertEquals(0L, state.getTotalExtendedSeconds(), "State mới phải là dữ liệu mặc định ban đầu");
    }

    @Test
    @DisplayName("getState: Gọi nhiều lần với cùng 1 roomId phải trả về ĐÚNG MỘT vùng nhớ (Singleton per room)")
    void getState_ExistingRoom_ReturnsSameInstance() {
        // Lấy lần 1
        AuctionRuntimeState firstCallState = AuctionStateManager.getState(TEST_ROOM_ID);

        // Lấy lần 2
        AuctionRuntimeState secondCallState = AuctionStateManager.getState(TEST_ROOM_ID);

        // assertSame dùng để kiểm tra xem 2 biến có trỏ về cùng một object trong RAM hay không
        assertSame(firstCallState, secondCallState, "Nhiều lần gọi getState cho cùng 1 phòng phải trả về cùng một object");
    }

    @Test
    @DisplayName("removeState: Xóa thành công state của phòng khỏi hệ thống")
    void removeState_ExistingRoom_RemovesState() {
        // 1. Tạo state và thay đổi dữ liệu để đánh dấu
        AuctionRuntimeState originalState = AuctionStateManager.getState(TEST_ROOM_ID);
        originalState.extendBySeconds(50);

        // 2. Tiến hành xóa state
        AuctionStateManager.removeState(TEST_ROOM_ID);

        // 3. Gọi lại getState lần nữa. Lúc này hệ thống phải tạo ra một object hoàn toàn mới
        AuctionRuntimeState newState = AuctionStateManager.getState(TEST_ROOM_ID);

        assertNotSame(originalState, newState, "State mới lấy ra phải là một object hoàn toàn khác với state đã bị xóa");
        assertEquals(0L, newState.getTotalExtendedSeconds(), "State mới lấy ra phải mang giá trị 0 ban đầu");
    }

    @Test
    @DisplayName("removeState: Không bị crash hoặc ném lỗi khi xóa một phòng không tồn tại")
    void removeState_NonExistentRoom_NoExceptionThrown() {
        // ConcurrentHashMap cho phép remove key không tồn tại một cách an toàn
        assertDoesNotThrow(() -> {
            AuctionStateManager.removeState("GHOST_ROOM");
        }, "Xóa một phòng không tồn tại không được ném ra lỗi Exception");
    }
}