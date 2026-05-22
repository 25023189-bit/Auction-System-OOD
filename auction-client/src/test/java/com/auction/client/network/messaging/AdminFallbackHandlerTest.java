package com.auction.client.network.messaging;

import com.auction.client.feature.controllers.account.admin.AdminController;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AdminFallbackHandlerTest {

    // === PHÒNG HỜ LỖI TOOLKIT KHI MOCK ADMIN CONTROLLER ===
    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    private SessionStore mockSessionStore;
    private AdminController mockAdminController;
    private AdminFallbackHandler handler;

    @BeforeEach
    void setUp() {
        mockSessionStore = mock(SessionStore.class);
        mockAdminController = mock(AdminController.class);
        handler = new AdminFallbackHandler(mockSessionStore);
    }

    // ==========================================
    // NHÓM TEST 1: KIỂM TRA BỘ LỌC SUPPORTS
    // ==========================================

    @Test
    @DisplayName("Hàm supports: Phải chấp nhận 6 action quy định")
    void testSupports_ValidActions_ReturnsTrue() {
        assertTrue(handler.supports("ADMIN_USER_LIST"));
        assertTrue(handler.supports("ADMIN_AUCTION_LIST"));
        assertTrue(handler.supports("ADMIN_PENDING_AUCTION_LIST"));
        assertTrue(handler.supports("BID_HISTORY_SUCCESS"));
        assertTrue(handler.supports("ADMIN_ACTION_SUCCESS"));
        assertTrue(handler.supports("ADMIN_ACTION_FAIL"));
    }

    @Test
    @DisplayName("Hàm supports: Phải từ chối các action rác hoặc rỗng")
    void testSupports_InvalidActions_ReturnsFalse() {
        assertFalse(handler.supports("CHAT_MSG"));
        assertFalse(handler.supports("LOGIN_SUCCESS"));
        assertFalse(handler.supports(""));
        assertFalse(handler.supports("XYZ_ACTION"));
    }

    // ==========================================
    // NHÓM TEST 2: KIỂM TRA LUỒNG HANDLE BỊ CHẶN
    // ==========================================

    @Test
    @DisplayName("Hàm handle: Nếu Admin Dashboard chưa mở (Controller = null) -> Không làm gì cả")
    void testHandle_NullAdminController_DoesNothing() {
        // Giả lập Dashboard chưa được bật
        when(mockSessionStore.getAdminController()).thenReturn(null);

        Message msg = new Message("ADMIN_USER_LIST", "SERVER", new ArrayList<User>());
        handler.handle(msg);

        // Xác thực không có bất kỳ lệnh nào được gọi xuống Controller (Vì nó có tồn tại đâu mà gọi)
        verifyNoInteractions(mockAdminController);
    }

    // ==========================================
    // NHÓM TEST 3: KIỂM TRA CÁC NHÁNH CẬP NHẬT UI
    // ==========================================

    @Test
    @DisplayName("Nhánh ADMIN_USER_LIST: Đẩy danh sách User vào bảng")
    void testHandle_AdminUserList() {
        when(mockSessionStore.getAdminController()).thenReturn(mockAdminController);

        List<User> mockUsers = new ArrayList<>();
        Message msg = new Message("ADMIN_USER_LIST", "SERVER", mockUsers);

        handler.handle(msg);

        verify(mockAdminController).updateUsersTable(mockUsers);
    }

    @Test
    @DisplayName("Nhánh ADMIN_AUCTION_LIST: Đẩy danh sách Phòng đấu giá vào bảng")
    void testHandle_AdminAuctionList() {
        when(mockSessionStore.getAdminController()).thenReturn(mockAdminController);

        List<AuctionRoom> mockRooms = new ArrayList<>();
        Message msg = new Message("ADMIN_AUCTION_LIST", "SERVER", mockRooms);

        handler.handle(msg);

        verify(mockAdminController).updateAuctionsTable(mockRooms);
    }

    @Test
    @DisplayName("Nhánh ADMIN_PENDING_AUCTION_LIST: Đẩy danh sách Chờ duyệt vào bảng")
    void testHandle_AdminPendingAuctionList() {
        when(mockSessionStore.getAdminController()).thenReturn(mockAdminController);

        List<PendingAuctionRequest> mockRequests = new ArrayList<>();
        Message msg = new Message("ADMIN_PENDING_AUCTION_LIST", "SERVER", mockRequests);

        handler.handle(msg);

        verify(mockAdminController).updatePendingAuctionsTable(mockRequests);
    }

    @Test
    @DisplayName("Nhánh BID_HISTORY_SUCCESS: Đẩy lịch sử đấu giá vào bảng")
    void testHandle_BidHistorySuccess() {
        when(mockSessionStore.getAdminController()).thenReturn(mockAdminController);

        List<BidTransaction> mockHistory = new ArrayList<>();
        Message msg = new Message("BID_HISTORY_SUCCESS", "SERVER", mockHistory);

        handler.handle(msg);

        verify(mockAdminController).updateBidHistoryTable(mockHistory);
    }

    @Test
    @DisplayName("Nhánh ADMIN_ACTION_SUCCESS: Gộp Action và ID lại để gửi phản hồi")
    void testHandle_AdminActionSuccess() {
        when(mockSessionStore.getAdminController()).thenReturn(mockAdminController);

        // Tạo Message có id là "U123" và data là thông báo thành công
        Message msg = new Message("ADMIN_ACTION_SUCCESS", "U123", "Đã xóa User thành công");

        handler.handle(msg);

        // Xác thực thuật toán gộp chuỗi: msg.getAction() + "_" + msg.id
        verify(mockAdminController).handleAdminResponse("ADMIN_ACTION_SUCCESS_U123", "Đã xóa User thành công");
    }

    @Test
    @DisplayName("Nhánh ADMIN_ACTION_FAIL: Ép cứng action thành chữ FAIL")
    void testHandle_AdminActionFail() {
        when(mockSessionStore.getAdminController()).thenReturn(mockAdminController);

        Message msg = new Message("ADMIN_ACTION_FAIL", "U123", "Lỗi mất mạng");

        handler.handle(msg);

        // Xác thực thuật toán: Đối số đầu tiên luôn bị ép thành "FAIL"
        verify(mockAdminController).handleAdminResponse("FAIL", "Lỗi mất mạng");
    }
}
