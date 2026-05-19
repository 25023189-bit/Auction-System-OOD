package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.IAuctionDAO;
import com.auction.server.dao.IUserDAO;
import com.auction.server.dao.IBidDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionRoomServiceTest {

    @Mock
    private IAuctionDAO mockAuctionDAO;

    @Mock
    private IUserDAO mockUserDAO;

    @Mock
    private IBidDAO mockBidDAO;

    @InjectMocks
    private AuctionRoomService roomService;

    private AuctionRoom sampleRoom;
    private User sampleUser;
    private AuctionRuntimeState mockState;

    @BeforeEach
    void setUp() {
        // Chuẩn bị User mẫu
        sampleUser = new User();
        sampleUser.setCustomerId("U001");
        sampleUser.setUsername("testuser");
        sampleUser.setRole("BIDDER");
        sampleUser.setBalance(1000.0);

        // Chuẩn bị Phòng đấu giá mẫu (Đang mở, thời gian hợp lệ)
        sampleRoom = new AuctionRoom();
        sampleRoom.setStatus("RUNNING");
        sampleRoom.setStartTime(LocalDateTime.now().minusMinutes(5));
        sampleRoom.setEndTime(LocalDateTime.now().plusMinutes(5));
        sampleRoom.setMinimumJoinAmount(100.0);
        sampleRoom.setExtensionSeconds(30);

        // Chuẩn bị Mock State
        mockState = mock(AuctionRuntimeState.class);
        lenient().when(mockState.getTotalExtendedSeconds()).thenReturn(0L);
        lenient().when(mockState.getParticipants()).thenReturn(Collections.emptySet());
    }

    // ==========================================
    // TEST NGHIỆP VỤ JOIN ROOM
    // ==========================================

    @Test
    @DisplayName("Join Room: Thất bại vì không tìm thấy phòng")
    void joinRoom_RoomNotFound_ReturnsFail() {
        when(mockAuctionDAO.getAuctionById("R001")).thenReturn(null);

        Message response = roomService.joinRoom("R001", "U001");

        assertEquals("ROOM_FAIL", response.getAction());
        assertTrue(response.getData().toString().contains("Room not found"));
    }

    @Test
    @DisplayName("Join Room: Thành công khi mọi điều kiện hợp lệ")
    void joinRoom_ValidConditions_ReturnsSuccess() {
        when(mockAuctionDAO.getAuctionById("R001")).thenReturn(sampleRoom);
        when(mockUserDAO.getUserById("U001")).thenReturn(sampleUser);

        try (MockedStatic<AuctionStateManager> stateManagerMock = mockStatic(AuctionStateManager.class)) {
            stateManagerMock.when(() -> AuctionStateManager.getState("R001")).thenReturn(mockState);

            Message response = roomService.joinRoom("R001", "U001");

            assertEquals("ROOM_JOINED", response.getAction());
            verify(mockState).addParticipant("U001");
        }
    }

    @Test
    @DisplayName("Join Room: Bị chặn vì tài khoản không đủ tiền")
    void joinRoom_InsufficientBalance_ReturnsFail() {
        sampleUser.setBalance(50.0);
        sampleRoom.setMinimumJoinAmount(100.0);

        when(mockAuctionDAO.getAuctionById("R001")).thenReturn(sampleRoom);
        when(mockUserDAO.getUserById("U001")).thenReturn(sampleUser);

        try (MockedStatic<AuctionStateManager> stateManagerMock = mockStatic(AuctionStateManager.class)) {
            stateManagerMock.when(() -> AuctionStateManager.getState("R001")).thenReturn(mockState);

            Message response = roomService.joinRoom("R001", "U001");

            assertEquals("ROOM_FAIL", response.getAction());
            assertTrue(response.getData().toString().contains("balance does not meet the minimum"));
        }
    }

    @Test
    @DisplayName("Join Room: Bị khóa vì vào phòng ở 30 giây cuối cùng")
    void joinRoom_FinalWindowLocked_ReturnsFail() {
        sampleRoom.setEndTime(LocalDateTime.now().plusSeconds(10));

        when(mockAuctionDAO.getAuctionById("R001")).thenReturn(sampleRoom);
        when(mockUserDAO.getUserById("U001")).thenReturn(sampleUser);

        when(mockState.isEntryLocked()).thenReturn(true);
        when(mockState.hasParticipant("U001")).thenReturn(false);

        try (MockedStatic<AuctionStateManager> stateManagerMock = mockStatic(AuctionStateManager.class)) {
            stateManagerMock.when(() -> AuctionStateManager.getState("R001")).thenReturn(mockState);

            Message response = roomService.joinRoom("R001", "U001");

            assertEquals("ROOM_FAIL", response.getAction());
            assertTrue(response.getData().toString().contains("locked during the final 30 seconds"));
        }
    }

    // ==========================================
    // TEST NGHIỆP VỤ PLACE NEW BID
    // ==========================================

    @Test
    @DisplayName("Place Bid: Thành công (Không rơi vào 30s cuối)")
    void placeNewBid_ValidBid_ReturnsSuccess() {
        when(mockUserDAO.getUserById("U001")).thenReturn(sampleUser);
        when(mockAuctionDAO.getAuctionById("R001")).thenReturn(sampleRoom);
        when(mockState.hasParticipant("U001")).thenReturn(true);

        // SỬA CHỮA Ở ĐÂY: Sử dụng trực tiếp đối tượng BidResult thay vì dùng Mock
        when(mockBidDAO.placeBid("R001", "U001", 1500.0)).thenReturn(IBidDAO.BidResult.success());

        try (MockedStatic<AuctionStateManager> stateManagerMock = mockStatic(AuctionStateManager.class)) {
            stateManagerMock.when(() -> AuctionStateManager.getState("R001")).thenReturn(mockState);

            Message response = roomService.placeNewBid("R001", "U001", 1500.0);

            assertEquals("BID_SUCCESS", response.getAction());
            assertEquals("testuser", response.getId());
            assertEquals(1500.0, sampleRoom.getCurrentPrice());
        }
    }

    @Test
    @DisplayName("Place Bid: Thất bại do không phải là BIDDER")
    void placeNewBid_NotBidderRole_ReturnsFail() {
        sampleUser.setRole("SELLER");
        when(mockUserDAO.getUserById("U001")).thenReturn(sampleUser);

        Message response = roomService.placeNewBid("R001", "U001", 1500.0);

        assertEquals("BID_FAIL", response.getAction());
        assertEquals("Only bidders can place bids.", response.getData());
    }
}