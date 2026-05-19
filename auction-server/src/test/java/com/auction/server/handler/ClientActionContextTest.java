package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.AuthService;
import com.auction.server.service.PendingAuctionApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientActionContextTest {

    private ClientActionContext context;
    private Consumer<Message> mockResponder;
    private ServerEventPublisher mockEventPublisher;
    private AuthService mockAuthService;
    private AuctionRoomService mockRoomService;
    private PendingAuctionApprovalService mockPendingService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock sạch các Service và Interface phụ thuộc đầu vào
        mockResponder = mock(Consumer.class);
        mockEventPublisher = mock(ServerEventPublisher.class);
        mockAuthService = mock(AuthService.class);
        mockRoomService = mock(AuctionRoomService.class);
        mockPendingService = mock(PendingAuctionApprovalService.class);

        // Khởi tạo đối tượng Context thật để kiểm tra logic
        context = new ClientActionContext(
                mockResponder,
                mockEventPublisher,
                mockAuthService,
                mockRoomService,
                mockPendingService
        );
    }

    @Test
    @DisplayName("Test khởi tạo Context thành công và kiểm tra Getters của các Service")
    void testContextInitializationAndServiceGetters() {
        assertNotNull(context);
        assertEquals(mockAuthService, context.getAuthService());
        assertEquals(mockRoomService, context.getRoomService());
        assertEquals(mockPendingService, context.getPendingAuctionApprovalService());
    }

    @Test
    @DisplayName("Test quản lý trạng thái UserId và CurrentRoomId (Get/Set/Clear)")
    void testSessionStateManagement() {
        // 1. Kiểm tra giá trị mặc định lúc mới tạo phải là chuỗi rỗng
        assertEquals("", context.getUserId());
        assertEquals("", context.getCurrentRoomId());

        // 2. Test Set/Get UserId (Xử lý trường hợp null luôn để ăn Branch Coverage)
        context.setUserId("USER_HAN_TO");
        assertEquals("USER_HAN_TO", context.getUserId());

        context.setUserId(null);
        assertEquals("", context.getUserId(), "Nếu set null thì hệ thống phải tự quy về chuỗi rỗng");

        // 3. Test Set/Get CurrentRoomId
        context.setCurrentRoomId("ROOM_AUCTION_1");
        assertEquals("ROOM_AUCTION_1", context.getCurrentRoomId());

        context.setCurrentRoomId(null);
        assertEquals("", context.getCurrentRoomId(), "Nếu set null phòng thì phải tự quy về chuỗi rỗng");

        // 4. Test hàm xóa phòng (clearCurrentRoom)
        context.setCurrentRoomId("ROOM_99");
        context.clearCurrentRoom();
        assertEquals("", context.getCurrentRoomId(), "Hàm clearCurrentRoom phải đưa mã phòng về rỗng");
    }

    @Test
    @DisplayName("Test hàm rời phòng có điều kiện (leaveCurrentRoomIfMatches)")
    void testLeaveCurrentRoomIfMatches() {
        // Trường hợp 1: Client đang ở phòng ROOM_A, hệ thống yêu cầu rời phòng ROOM_B (Không khớp) -> Không rời
        context.setCurrentRoomId("ROOM_A");
        context.leaveCurrentRoomIfMatches("ROOM_B");
        assertEquals("ROOM_A", context.getCurrentRoomId(), "Không trùng mã phòng thì không được kích hoạt xóa");

        // Trường hợp 2: Truyền vào giá trị null -> Không rời
        context.leaveCurrentRoomIfMatches(null);
        assertEquals("ROOM_A", context.getCurrentRoomId());

        // Trường hợp 3: Khớp mã phòng ROOM_A -> Rời phòng thành công (về chuỗi rỗng)
        context.leaveCurrentRoomIfMatches("ROOM_A");
        assertEquals("", context.getCurrentRoomId(), "Trùng mã phòng thì session phòng phải bị xóa sạch");
    }

    @Test
    @DisplayName("Test toàn bộ các hàm chuyển tiếp thông điệp mạng và phát Event")
    void testMessageAndEventDelegation() {
        Message sampleMsg = new Message("TEST_ACTION", "DATA");

        // 1. Test hàm send() trực tiếp về máy client hiện tại
        context.send(sampleMsg);
        verify(mockResponder, times(1)).accept(sampleMsg);

        // 2. Test hàm broadcastAll() toàn server
        context.broadcastAll(sampleMsg);
        verify(mockEventPublisher, times(1)).broadcastAll(sampleMsg);

        // 3. Test hàm broadcastToRoom() gửi riêng cho một phòng
        context.broadcastToRoom("ROOM_101", sampleMsg);
        verify(mockEventPublisher, times(1)).broadcastToRoom("ROOM_101", sampleMsg);

        // 4. Test hàm notifyRoomClosed() báo sập phòng
        context.notifyRoomClosed("ROOM_101");
        verify(mockEventPublisher, times(1)).notifyRoomClosed("ROOM_101");

        // 5. Test hàm notifyDeletedUser() báo khóa tài khoản
        context.notifyDeletedUser("USER_99");
        verify(mockEventPublisher, times(1)).notifyDeletedUser("USER_99");
    }
}