package com.auction.server.handler;

import com.auction.client.autoApprove.AuctionAiAutoApproveConnector;
import com.auction.client.autoApprove.AutoApproveListingInput;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.PendingAuctionApprovalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SellerActionHandlerTest {

    private SellerActionHandler handler;
    private ClientActionContext mockContext;
    private PendingAuctionApprovalService mockApprovalService;

    // Các biến dùng để mock constructor của DAO giải lập DB
    private MockedConstruction<UserDAO> mockedUserDAO;
    private MockedConstruction<AuctionDAO> mockedAuctionDAO;

    @BeforeEach
    void setUp() {
        AuctionAiAutoApproveConnector fakeAutoApproveConnector = new AuctionAiAutoApproveConnector() {
            @Override
            public boolean requestDecision(AutoApproveListingInput input) {
                return false;
            }
        };
        handler = new SellerActionHandler(fakeAutoApproveConnector, new PendingAuctionRoomFactory());
        mockContext = mock(ClientActionContext.class);
        mockApprovalService = mock(PendingAuctionApprovalService.class);

        // Kết nối service giả lập vào ngữ cảnh kết nối
        when(mockContext.getPendingAuctionApprovalService()).thenReturn(mockApprovalService);
    }

    @AfterEach
    void tearDown() {
        // Đóng mock constructor sau khi test xong để không ảnh hưởng bài khác
        if (mockedUserDAO != null) mockedUserDAO.close();
        if (mockedAuctionDAO != null) mockedAuctionDAO.close();
    }

    @Test
    @DisplayName("Test dữ liệu gửi lên bị thiếu trường hoặc sai định dạng")
    void testHandleCreateAuction_InvalidDataLength() {
        // Form chuẩn cần 8 trường cách nhau bởi dấu |, ở đây cố tình gửi thiếu
        Message message = new Message("CREATE_AUCTION", "SELLER_01", "ItemName|Description|100.0");

        handler.handle(message, mockContext);

        // Kiểm tra xem hệ thống có gửi tin nhắn báo lỗi về cho Client không
        ArgumentCaptor<Message> responseCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext).send(responseCaptor.capture());
        assertEquals("CREATE_AUCTION_FAIL", responseCaptor.getValue().getAction());
        assertEquals("Invalid auction creation data!", responseCaptor.getValue().getData());
    }

    @Test
    @DisplayName("Test trường hợp không tìm thấy tài khoản người bán")
    void testHandleCreateAuction_SellerNotFound() {
        // Giả lập chuỗi form hợp lệ 8 trường nhưng ID người bán không tồn tại trong hệ thống
        String validPayload = "Tranh cổ|Đồ hiếm|500.0|50.0|10.0|" + LocalDateTime.now().plusDays(1) + "|60|10";
        Message message = new Message("CREATE_AUCTION", "NON_EXIST_ID", validPayload);

        // Mock Constructor của UserDAO để khi code khởi tạo 'new UserDAO()' nó sẽ trả về kết quả rỗng (null)
        mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById("NON_EXIST_ID")).thenReturn(null);
        });

        handler.handle(message, mockContext);

        ArgumentCaptor<Message> responseCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext).send(responseCaptor.capture());
        assertEquals("CREATE_AUCTION_FAIL", responseCaptor.getValue().getAction());
        assertTrue(responseCaptor.getValue().getData().toString().contains("Seller account not found"));
    }

    @Test
    @DisplayName("Test trường hợp tài khoản có tồn tại nhưng sai vai trò (Không phải SELLER)")
    void testHandleCreateAuction_WrongRole() {
        String validPayload = "Tranh cổ|Đồ hiếm|500.0|50.0|10.0|" + LocalDateTime.now().plusDays(1) + "|60|10";
        Message message = new Message("CREATE_AUCTION", "BIDDER_01", validPayload);

        // Tạo một User thật nhưng gán role là BIDDER
        User wrongUser = new User();
        wrongUser.setCustomerId("BIDDER_01");
        wrongUser.setRole("BIDDER");

        mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById("BIDDER_01")).thenReturn(wrongUser);
        });

        handler.handle(message, mockContext);

        ArgumentCaptor<Message> responseCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext).send(responseCaptor.capture());
        assertEquals("CREATE_AUCTION_FAIL", responseCaptor.getValue().getAction());
        assertTrue(responseCaptor.getValue().getData().toString().contains("Only sellers can create auctions"));
    }

    @Test
    @DisplayName("Test luồng tạo yêu cầu đấu giá thành công và đưa vào hàng chờ duyệt")
    void testHandleCreateAuction_Success() {
        // Thiết lập thời gian bắt đầu ở tương lai để vượt qua vòng xác thực luật tạo phòng
        String futureTimeStr = LocalDateTime.now().plusDays(2).toString();
        String validPayload = "Bức Tranh Đêm|Mô tả xịn|1000.0|200.0|50.0|" + futureTimeStr + "|120|30";
        Message message = new Message("CREATE_AUCTION", "SELLER_OK", validPayload);

        // 1. Giả lập thông tin người bán hợp lệ
        User validSeller = new User();
        validSeller.setCustomerId("SELLER_OK");
        validSeller.setRole("SELLER");
        validSeller.setSellerReputation(5.0);
        validSeller.setOrganization("UET_GALLERY");

        mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById("SELLER_OK")).thenReturn(validSeller);
        });

        // 2. Giả lập thống kê phòng của AuctionDAO (Ép tỷ lệ hủy = 0%, tỷ lệ thành công = 100%)
        mockedAuctionDAO = mockConstruction(AuctionDAO.class, (mock, context) -> {
            AuctionDAO.SellerAuctionStats mockStats = mock(AuctionDAO.SellerAuctionStats.class);
            when(mockStats.getSuccessfulAuctionRate()).thenReturn(1.0);
            when(mockStats.getAdminCancellationRate()).thenReturn(0.0);
            when(mock.getSellerAuctionStats("SELLER_OK")).thenReturn(mockStats);
            when(mock.generateNextAuctionId()).thenReturn("AU100001");
        });

        handler.handle(message, mockContext);

        // 3. Kiểm chứng xem hệ thống có đẩy request thành công vào trạng thái PENDING không
        ArgumentCaptor<Message> responseCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext, atLeastOnce()).send(responseCaptor.capture());

        // Tìm gói tin phản hồi có chứa trạng thái chờ duyệt
        boolean hasPendingResponse = responseCaptor.getAllValues().stream()
                .anyMatch(msg -> "CREATE_AUCTION_PENDING".equals(msg.getAction()));

        assertTrue(hasPendingResponse, "Hệ thống phải trả về mã lệnh CREATE_AUCTION_PENDING báo thành công");

        // Kiểm chứng xem hàm submit lên hàng chờ duyệt có được gọi không
        verify(mockApprovalService, times(1)).submit(any());
    }

    @Test
    @DisplayName("Test AI auto approve true tạo phiên thật và không đưa vào hàng chờ admin")
    void testHandleCreateAuction_AutoApproveSuccess() {
        AuctionAiAutoApproveConnector autoApproveTrueConnector = new AuctionAiAutoApproveConnector() {
            @Override
            public boolean requestDecision(AutoApproveListingInput input) {
                return true;
            }
        };
        handler = new SellerActionHandler(autoApproveTrueConnector, new PendingAuctionRoomFactory());

        String futureTimeStr = LocalDateTime.now().plusDays(2).toString();
        String validPayload = "May anh co|Mo ta day du|1000.0|200.0|50.0|" + futureTimeStr + "|120|30";
        Message message = new Message("CREATE_AUCTION", "SELLER_OK", validPayload);

        User validSeller = new User();
        validSeller.setCustomerId("SELLER_OK");
        validSeller.setRole("SELLER");
        validSeller.setSellerReputation(5.0);
        validSeller.setOrganization("UET_GALLERY");

        mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById("SELLER_OK")).thenReturn(validSeller);
        });

        mockedAuctionDAO = mockConstruction(AuctionDAO.class, (mock, context) -> {
            AuctionDAO.SellerAuctionStats mockStats = mock(AuctionDAO.SellerAuctionStats.class);
            when(mockStats.getSuccessfulAuctionRate()).thenReturn(1.0);
            when(mockStats.getAdminCancellationRate()).thenReturn(0.0);
            when(mock.getSellerAuctionStats("SELLER_OK")).thenReturn(mockStats);
            when(mock.generateNextAuctionId()).thenReturn("AU100001");
            when(mock.createAuctionWithItem(any(), any(), eq("SELLER_OK"))).thenReturn(true);
            when(mock.getAllActiveAuctions()).thenReturn(java.util.List.of());
        });

        handler.handle(message, mockContext);

        ArgumentCaptor<Message> responseCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockContext, atLeastOnce()).send(responseCaptor.capture());

        boolean hasSuccessResponse = responseCaptor.getAllValues().stream()
                .anyMatch(msg -> "CREATE_AUCTION_SUCCESS".equals(msg.getAction()));

        assertTrue(hasSuccessResponse);
        verify(mockApprovalService, never()).submit(any());
    }
}
