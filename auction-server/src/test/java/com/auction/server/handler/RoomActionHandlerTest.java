package com.auction.server.handler;

import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.ProductDetailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomActionHandlerTest {

    private RoomActionHandler handler;
    private ClientActionContext mockContext;
    private AuctionRoomService mockRoomService;

    // Quản lý đóng/mở mock các constructor khởi tạo local trong file gốc
    private MockedConstruction<AuctionDAO> mockedAuctionDAO;
    private MockedConstruction<UserDAO> mockedUserDAO;
    private MockedConstruction<TransactionDAO> mockedTransactionDAO;
    private MockedConstruction<ProductDetailService> mockedProductDetailService;

    @BeforeEach
    void setUp() {
        handler = new RoomActionHandler();
        mockContext = mock(ClientActionContext.class);
        mockRoomService = mock(AuctionRoomService.class);

        // Đóng đinh kết nối dịch vụ phòng vào ngữ cảnh kết nối
        when(mockContext.getRoomService()).thenReturn(mockRoomService);
        when(mockContext.getUserId()).thenReturn("USER_HAN");
    }

    @AfterEach
    void tearDown() {
        // Vét sạch dọn dẹp các mock constructor sau mỗi vòng test
        if (mockedAuctionDAO != null) mockedAuctionDAO.close();
        if (mockedUserDAO != null) mockedUserDAO.close();
        if (mockedTransactionDAO != null) mockedTransactionDAO.close();
        if (mockedProductDetailService != null) mockedProductDetailService.close();
    }

    @Test
    @DisplayName("Test xử lý Action lạ chưa được hệ thống hỗ trợ")
    void testHandle_UnknownAction() {
        Message message = new Message("HUY_DIET_SERVER", "Rác");
        handler.handle(message, mockContext);

        ArgumentCaptor<Message> responseCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext).send(responseCaptor.capture());
        assertEquals("UNKNOWN_ACTION", responseCaptor.getValue().getAction());
    }

    @Test
    @DisplayName("Test luồng JOIN_ROOM thành công và phát tín hiệu cập nhật phòng")
    void testHandleJoinRoom_Success() {
        Message message = new Message("JOIN_ROOM", "AU100001");
        when(mockContext.getCurrentRoomId()).thenReturn("AU100001");

        // Giả lập kết quả trả về từ nghiệp vụ phòng đấu giá là thành công
        Message successResponse = new Message("ROOM_JOINED", "SERVER", "RoomData");
        when(mockRoomService.joinRoom("AU100001", "USER_HAN")).thenReturn(successResponse);

        handler.handle(message, mockContext);

        // Kiểm tra xem phòng hiện tại có được ghi nhận vào Context không
        verify(mockContext).setCurrentRoomId("AU100001");
        verify(mockContext).send(successResponse);
        verify(mockContext).broadcastToRoom(eq("AU100001"), any());
    }

    @Test
    @DisplayName("Test luồng JOIN_ROOM thất bại do mã phòng trống hoặc lỗi nghiệp vụ")
    void testHandleJoinRoom_Fail() {
        // Case 1: Mã phòng gửi lên trống rỗng
        Message emptyMessage = new Message("JOIN_ROOM", "");
        handler.handle(emptyMessage, mockContext);

        verify(mockContext, atLeastOnce()).send(argThat(msg -> "ROOM_FAIL".equals(msg.getAction())));

        // Case 2: RoomService trả về mã lỗi ROOM_FAIL -> Phải lập tức clear session phòng
        Message failMessage = new Message("JOIN_ROOM", "ERROR_ROOM");
        when(mockContext.getCurrentRoomId()).thenReturn("ERROR_ROOM");
        when(mockRoomService.joinRoom("ERROR_ROOM", "USER_HAN")).thenReturn(new Message("ROOM_FAIL", "SERVER", "Lỗi"));

        handler.handle(failMessage, mockContext);
        verify(mockContext, atLeastOnce()).clearCurrentRoom();
    }

    @Test
    @DisplayName("Test luồng GET_ROOMS lấy danh sách phòng đang hoạt động ở sảnh đợi")
    void testHandleGetRooms_Success() {
        Message message = new Message("GET_ROOMS", null);

        // Đánh lừa constructor 'new AuctionDAO()' trả về list rỗng thay vì chọc vào DB thật
        mockedAuctionDAO = mockConstruction(AuctionDAO.class, (mock, context) -> {
            when(mock.getAllActiveAuctions()).thenReturn(Collections.emptyList());
        });

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "ROOM_LIST".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test đặt giá đấu (BID) thành công và đẩy giá mới lên toàn hệ thống")
    void testHandleBid_Success() {
        Message message = new Message("BID", 5000.0); // Client đặt giá 5000
        when(mockContext.getCurrentRoomId()).thenReturn("AU100001");

        Message bidSuccessResponse = new Message("BID_SUCCESS", "SERVER", "Payload");
        when(mockRoomService.placeNewBid("AU100001", "USER_HAN", 5000.0)).thenReturn(bidSuccessResponse);

        handler.handle(message, mockContext);

        // Xác thực đã phát giá mới cho người trong phòng và cập nhật thẻ phòng ở sảnh (Lobby)
        verify(mockContext).broadcastToRoom("AU100001", bidSuccessResponse);
        verify(mockContext).broadcastAll(argThat(msg -> "UPDATE_PRICE".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test tính năng gửi tin nhắn Chat trong phòng đấu giá")
    void testHandleChat_Success() {
        Message message = new Message("CHAT_MSG", "Hello mọi người!");
        when(mockContext.getCurrentRoomId()).thenReturn("AU100001");

        User mockUser = new User();
        mockUser.setUsername("ToBaoHan_UET");

        // Đánh lừa constructor 'new UserDAO()'
        mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById("USER_HAN")).thenReturn(mockUser);
        });

        handler.handle(message, mockContext);
        verify(mockContext).broadcastToRoom(eq("AU100001"), argThat(msg -> "CHAT_MSG".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test lấy lịch sử đặt giá của phòng (GET_BID_HISTORY)")
    void testHandleGetBidHistory_Success() {
        Message message = new Message("GET_BID_HISTORY", "AU100001");

        // Đánh lừa 'new TransactionDAO()'
        mockedTransactionDAO = mockConstruction(TransactionDAO.class, (mock, context) -> {
            when(mock.getHistoryByRoom("AU100001")).thenReturn(Collections.emptyList());
        });

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "BID_HISTORY_SUCCESS".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test lấy thông tin chi tiết sản phẩm và popup quảng cáo")
    void testHandleGetProductDetails_Success() {
        Message message = new Message("GET_PRODUCT_DETAILS", "AU100001");

        // Đánh lừa 'new ProductDetailService()'
        mockedProductDetailService = mockConstruction(ProductDetailService.class, (mock, context) -> {
            when(mock.getProductDetails("AU100001")).thenReturn(mock(com.auction.common.model.ProductDetailResponse.class));
        });

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "PRODUCT_DETAILS_SUCCESS".equals(msg.getAction())));
    }

    @Test
    void testHandleGetProductDetails_FailsForMissingRoomId() {
        handler.handle(new Message("GET_PRODUCT_DETAILS", "  "), mockContext);

        verify(mockContext).send(argThat(msg -> "PRODUCT_DETAILS_FAIL".equals(msg.getAction())));
    }

    @Test
    void testHandleGetProductDetails_FailsWhenServiceHasNoDetails() {
        mockedProductDetailService = mockConstruction(ProductDetailService.class, (mock, context) -> {
            when(mock.getProductDetails("MISSING")).thenReturn(null);
        });

        handler.handle(new Message("GET_PRODUCT_DETAILS", "MISSING"), mockContext);

        verify(mockContext).send(argThat(msg -> "PRODUCT_DETAILS_FAIL".equals(msg.getAction())));
    }

    @Test
    @DisplayName("SET_AUTO_BID thanh cong dung userId va khong trigger khi user dang thang")
    void testHandleSetAutoBid_SuccessWhenAlreadyHighestBidder() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("AUTO_ROOM_SET");
        room.setStatus("RUNNING");
        room.setCurrentPrice(1000.0);
        room.setBidStep(100.0);
        room.setHighestBidder("USER_HAN");

        when(mockContext.getCurrentRoomId()).thenReturn("AUTO_ROOM_SET");
        when(mockRoomService.getLiveRoom("AUTO_ROOM_SET")).thenReturn(room);

        handler.handle(new Message("SET_AUTO_BID", new AutoBidRequest("AUTO_ROOM_SET", 2000.0, 100.0)), mockContext);

        verify(mockContext).send(argThat(msg -> "AUTO_BID_SET_SUCCESS".equals(msg.getAction())));
        verify(mockRoomService, never()).placeNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("CANCEL_AUTO_BID tra success/fail ro rang")
    void testHandleCancelAutoBid_SuccessAndFailure() {
        AuctionRoom room = new AuctionRoom();
        room.setRoomId("AUTO_ROOM_CANCEL");
        room.setStatus("RUNNING");
        room.setCurrentPrice(1000.0);
        room.setBidStep(100.0);
        room.setHighestBidder("USER_HAN");

        when(mockContext.getCurrentRoomId()).thenReturn("AUTO_ROOM_CANCEL");
        when(mockRoomService.getLiveRoom("AUTO_ROOM_CANCEL")).thenReturn(room);

        handler.handle(new Message("SET_AUTO_BID", new AutoBidRequest("AUTO_ROOM_CANCEL", 2000.0, 100.0)), mockContext);
        handler.handle(new Message("CANCEL_AUTO_BID", "AUTO_ROOM_CANCEL"), mockContext);
        handler.handle(new Message("CANCEL_AUTO_BID", "AUTO_ROOM_CANCEL"), mockContext);

        verify(mockContext).send(argThat(msg -> "AUTO_BID_CANCEL_SUCCESS".equals(msg.getAction())));
        verify(mockContext).send(argThat(msg -> "AUTO_BID_CANCEL_FAILED".equals(msg.getAction())));
    }
}
