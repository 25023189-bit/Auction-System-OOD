package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.IAuctionDAO;
import com.auction.server.dao.IBidDAO;
import com.auction.server.dao.IUserDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuctionRoomServiceTest {

    private AuctionRoomService roomService;
    private IAuctionDAO mockAuctionDAO;
    private IUserDAO mockUserDAO;
    private IBidDAO mockBidDAO;

    private AuctionRoom mockRoom;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Giả lập các DAO
        mockAuctionDAO = Mockito.mock(IAuctionDAO.class);
        mockUserDAO = Mockito.mock(IUserDAO.class);
        mockBidDAO = Mockito.mock(IBidDAO.class);

        // Khởi tạo Service với các DAO giả lập
        roomService = new AuctionRoomService(mockAuctionDAO, mockUserDAO, mockBidDAO);

        // Chuẩn bị dữ liệu mẫu
        mockRoom = new AuctionRoom();
        mockRoom.setRoomId("ROOM_1");
        mockRoom.setStatus("RUNNING");
        mockRoom.setEndTime(LocalDateTime.now().plusMinutes(10)); // Còn 10 phút nữa mới hết giờ

        mockUser = new User();
        mockUser.setCustomerId("U1");
        mockUser.setUsername("hancute");
        mockUser.setRole("BIDDER");

        // Dạy cho các DAO giả lập cách trả lời
        when(mockAuctionDAO.getAuctionById("ROOM_1")).thenReturn(mockRoom);
        when(mockUserDAO.getUserById("U1")).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp RAM sau mỗi bài test
        AuctionStateManager.removeState("ROOM_1");
    }

    @Test
    @DisplayName("Join phòng thành công khi hợp lệ")
    void testJoinRoomSuccess() {
        Message result = roomService.joinRoom("ROOM_1", "U1");

        assertEquals("ROOM_JOINED", result.getAction());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("Báo lỗi khi Join phòng không tồn tại")
    void testJoinRoomFail_NotFound() {
        when(mockAuctionDAO.getAuctionById("ROOM_999")).thenReturn(null);

        Message result = roomService.joinRoom("ROOM_999", "U1");
        assertEquals("ROOM_FAIL", result.getAction());
        assertTrue(result.getData().toString().contains("Room not found"));
    }

    @Test
    @DisplayName("Đặt giá thành công")
    void testPlaceBidSuccess() {
        // Phải Join phòng trước thì mới được Bid (Theo logic của Hùng)
        roomService.joinRoom("ROOM_1", "U1");

        // Giả lập DB lưu giá thành công
        IBidDAO.BidResult mockResult = new IBidDAO.BidResult(true, "Success");
        when(mockBidDAO.placeBid(anyString(), anyString(), anyDouble())).thenReturn(mockResult);

        Message result = roomService.placeNewBid("ROOM_1", "U1", 5000.0);

        assertEquals("BID_SUCCESS", result.getAction());
        assertEquals("hancute", result.getId()); // Gắn tên người đặt giá
        assertEquals(5000.0, mockRoom.getCurrentPrice());
    }

    @Test
    @DisplayName("Khóa phòng không cho Join ở 30 giây cuối")
    void testLockEntryInFinalWindow() {
        // Chỉnh giờ phòng lại chỉ còn 15 giây
        mockRoom.setEndTime(LocalDateTime.now().plusSeconds(15));

        Message result = roomService.joinRoom("ROOM_1", "U1");

        assertEquals("ROOM_FAIL", result.getAction());
        assertTrue(result.getData().toString().contains("locked during the final 30 seconds"));
    }
}