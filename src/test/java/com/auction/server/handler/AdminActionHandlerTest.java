package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuctionStateManager;
import com.auction.server.service.PendingAuctionApprovalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminActionHandlerTest {

    private AdminActionHandler handler;
    private PendingAuctionRoomFactory mockFactory;
    private ClientActionContext mockContext;
    private PendingAuctionApprovalService mockApprovalService;

    // Quản lý mock constructor local và hàm static
    private MockedConstruction<UserDAO> mockUserDaoConstruction;
    private MockedConstruction<AuctionDAO> mockAuctionDaoConstruction;
    private MockedStatic<AuctionStateManager> mockStateManagerStatic;

    @BeforeEach
    void setUp() {
        mockFactory = mock(PendingAuctionRoomFactory.class);
        mockContext = mock(ClientActionContext.class);
        mockApprovalService = mock(PendingAuctionApprovalService.class);

        when(mockContext.getPendingAuctionApprovalService()).thenReturn(mockApprovalService);

        handler = new AdminActionHandler(mockFactory);
        // Bắt cóc class static quản lý trạng thái phòng
        mockStateManagerStatic = mockStatic(AuctionStateManager.class);
    }

    @AfterEach
    void tearDown() {
        if (mockUserDaoConstruction != null) mockUserDaoConstruction.close();
        if (mockAuctionDaoConstruction != null) mockAuctionDaoConstruction.close();
        if (mockStateManagerStatic != null) mockStateManagerStatic.close();
    }

    @Test
    @DisplayName("Test ADMIN_GET_USERS trả về danh sách toàn bộ người dùng")
    void testHandleAdminGetUsers() {
        Message message = new Message("ADMIN_GET_USERS", null);

        mockUserDaoConstruction = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getAllUsers()).thenReturn(Collections.emptyList());
        });

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "ADMIN_USER_LIST".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test ADMIN_GET_AUCTIONS lấy toàn bộ danh sách phòng đấu giá")
    void testHandleAdminGetAuctions() {
        Message message = new Message("ADMIN_GET_AUCTIONS", null);

        mockAuctionDaoConstruction = mockConstruction(AuctionDAO.class, (mock, context) -> {
            when(mock.getAllAuctions()).thenReturn(Collections.emptyList());
        });

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "ADMIN_AUCTION_LIST".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test ADMIN_GET_PENDING_AUCTIONS lấy danh sách hàng chờ duyệt")
    void testHandleAdminGetPendingAuctions() {
        Message message = new Message("ADMIN_GET_PENDING_AUCTIONS", null);
        when(mockApprovalService.getAllPending()).thenReturn(Collections.emptyList());

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "ADMIN_PENDING_AUCTION_LIST".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test duyệt yêu cầu tạo phiên (ADMIN_APPROVE_AUCTION) thành công")
    void testHandleAdminApproveAuction_Success() {
        Message message = new Message("ADMIN_APPROVE_AUCTION", "PA000001");

        PendingAuctionRequest mockRequest = new PendingAuctionRequest();
        mockRequest.setRoomId("AU112345"); // Format chuẩn khớp regex ^AU1\d{5}$
        mockRequest.setItemId("IT12345");
        mockRequest.setSellerId("SELLER_01");

        when(mockApprovalService.approve("PA000001")).thenReturn(mockRequest);

        AuctionRoom mockRoom = mock(AuctionRoom.class);
        Item mockItem = mock(Item.class);
        when(mockFactory.createRoom(mockRequest)).thenReturn(mockRoom);
        when(mockFactory.createItem(mockRequest)).thenReturn(mockItem);

        mockAuctionDaoConstruction = mockConstruction(AuctionDAO.class, (mock, context) -> {
            when(mock.createAuctionWithItem(mockRoom, mockItem, "SELLER_01")).thenReturn(true);
        });

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "ADMIN_ACTION_SUCCESS".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test duyệt yêu cầu tạo phiên với RoomId sai định dạng (Tự sinh ID mới)")
    void testHandleAdminApproveAuction_InvalidRoomIdFormat() {
        Message message = new Message("ADMIN_APPROVE_AUCTION", "PA000001");

        PendingAuctionRequest mockRequest = new PendingAuctionRequest();
        mockRequest.setRoomId("INVALID_FORMAT_ID"); // Không khớp regex
        mockRequest.setSellerId("SELLER_01");

        when(mockApprovalService.approve("PA000001")).thenReturn(mockRequest);

        mockAuctionDaoConstruction = mockConstruction(AuctionDAO.class, (mock, context) -> {
            when(mock.generateNextAuctionId()).thenReturn("AU199999");
            when(mock.createAuctionWithItem(any(), any(), eq("SELLER_01"))).thenReturn(true);
        });

        handler.handle(message, mockContext);
        assertEquals("AU199999", mockRequest.getRoomId(), "Hệ thống phải tự sinh ID chuẩn đè lên ID lỗi");
    }

    @Test
    @DisplayName("Test từ chối yêu cầu duyệt tạo phiên (ADMIN_REJECT_AUCTION)")
    void testHandleAdminRejectAuction() {
        Message message = new Message("ADMIN_REJECT_AUCTION", "PA000001");
        when(mockApprovalService.reject("PA000001")).thenReturn(true);

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "ADMIN_ACTION_SUCCESS".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test Admin cưỡng ép sập phòng đấu giá dang dở (ADMIN_DELETE_AUCTION)")
    void testHandleAdminDeleteAuction() {
        Message message = new Message("ADMIN_DELETE_AUCTION", "AU112345");

        mockAuctionDaoConstruction = mockConstruction(AuctionDAO.class, (mock, context) -> {
            when(mock.forceDeleteAuction("AU112345")).thenReturn(true);
        });

        handler.handle(message, mockContext);

        // Kiểm tra xem runtime state trong bộ nhớ và client trong phòng có bị dọn dẹp không
        mockStateManagerStatic.verify(() -> AuctionStateManager.removeState("AU112345"), times(1));
        verify(mockContext).notifyRoomClosed("AU112345");
    }

    @Test
    @DisplayName("Test Admin xóa tài khoản người dùng vi phạm (ADMIN_DELETE_USER)")
    void testHandleAdminDeleteUser() {
        Message message = new Message("ADMIN_DELETE_USER", "USER_BAD_01");

        mockUserDaoConstruction = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.deleteUser("USER_BAD_01")).thenReturn(true);
        });

        handler.handle(message, mockContext);

        // Đảm bảo hệ thống đá người dùng ra khỏi cụm kết nối online
        verify(mockContext).notifyDeletedUser("USER_BAD_01");
    }
}