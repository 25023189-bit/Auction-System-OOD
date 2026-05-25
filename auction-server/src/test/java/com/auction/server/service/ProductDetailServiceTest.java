package com.auction.server.service;

import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.ProductDetailResponse;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private AuctionDAO mockAuctionDAO;

    @Mock
    private TransactionDAO mockTransactionDAO;

    @InjectMocks
    private ProductDetailService productDetailService;

    private AuctionRoom sampleRoom;

    @BeforeEach
    void setUp() {
        sampleRoom = new AuctionRoom();
        sampleRoom.setItemName("MacBook M3 Pro");
        sampleRoom.setItemDescription("Hàng likenew 99%");
        sampleRoom.setCurrentPrice(2500.0);
    }

    @Test
    @DisplayName("Lấy chi tiết: Trả về null ngay lập tức nếu không tìm thấy phòng đấu giá")
    void getProductDetails_RoomNotFound_ReturnsNull() {
        when(mockAuctionDAO.getAuctionById("ROOM_INVALID")).thenReturn(null);

        ProductDetailResponse response = productDetailService.getProductDetails("ROOM_INVALID");

        assertNull(response, "Phải trả về null nếu room không tồn tại");

        // Đảm bảo không gọi xuống TransactionDAO để tiết kiệm tài nguyên
        verify(mockTransactionDAO, never()).getHistoryByRoom(anyString());
    }

    @Test
    @DisplayName("Lấy chi tiết: Tính toán thời gian còn lại (TimeLeftMillis) chính xác khi phòng đang mở")
    void getProductDetails_FutureEndTime_CalculatesTimeLeftCorrectly() {
        // Đặt EndTime ở tương lai (1 phút nữa)
        sampleRoom.setEndTime(LocalDateTime.now().plusMinutes(1));

        when(mockAuctionDAO.getAuctionById("ROOM_001")).thenReturn(sampleRoom);
        when(mockTransactionDAO.getHistoryByRoom("ROOM_001")).thenReturn(null);

        ProductDetailResponse response = productDetailService.getProductDetails("ROOM_001");

        assertNotNull(response);
        assertEquals("MacBook M3 Pro", response.getTitle());
        assertEquals(2500.0, response.getCurrentPrice());

        // Trả về thời gian còn lại phải là một số dương (> 0)
        assertTrue(response.getTimeLeftMillis() > 0, "Thời gian còn lại của phòng đang chạy phải lớn hơn 0");
        assertTrue(response.getBidHistory().isEmpty(), "Danh sách bid history phải trống nếu DB trả về null");
    }

    @Test
    @DisplayName("Lấy chi tiết: Trả về 0 mili-giây nếu EndTime bị Null hoặc phòng Đã Hết Hạn")
    void getProductDetails_NullOrPastEndTime_ReturnsZeroTimeLeft() {
        // Trường hợp 1: EndTime = null
        sampleRoom.setEndTime(null);
        when(mockAuctionDAO.getAuctionById("ROOM_002")).thenReturn(sampleRoom);

        ProductDetailResponse responseNullTime = productDetailService.getProductDetails("ROOM_002");
        assertEquals(0, responseNullTime.getTimeLeftMillis(), "EndTime null phải trả về 0 millis");

        // Trường hợp 2: EndTime trong quá khứ (đã hết giờ)
        sampleRoom.setEndTime(LocalDateTime.now().minusMinutes(5));

        ProductDetailResponse responsePastTime = productDetailService.getProductDetails("ROOM_002");
        assertEquals(0, responsePastTime.getTimeLeftMillis(), "EndTime quá khứ phải trả về 0 millis");
    }

    @Test
    @DisplayName("Lấy chi tiết: Map lịch sử đấu giá (Bid History) từ Transaction thành DTO chuẩn xác")
    void getProductDetails_WithTransactions_MapsToDTOsCorrectly() {
        sampleRoom.setEndTime(LocalDateTime.now().plusMinutes(5));
        when(mockAuctionDAO.getAuctionById("ROOM_003")).thenReturn(sampleRoom);

        // Tạo 2 giao dịch giả lập
        BidTransaction tx1 = new BidTransaction();
        tx1.setBidderId("U001"); // Có ID rõ ràng
        tx1.setBidAmount(2600.0);
        tx1.setBidTime("2026-05-19 10:00:00");

        BidTransaction tx2 = new BidTransaction();
        tx2.setBidderId(null);   // Khách vãng lai (ẩn danh)
        tx2.setBidAmount(2700.0);
        tx2.setBidTime("2026-05-19 10:05:00");

        when(mockTransactionDAO.getHistoryByRoom("ROOM_003")).thenReturn(Arrays.asList(tx1, tx2));

        ProductDetailResponse response = productDetailService.getProductDetails("ROOM_003");

        assertNotNull(response);
        List<BidHistoryDTO> history = response.getBidHistory();

        assertEquals(2, history.size(), "Phải map đúng 2 giao dịch");

        // Kiểm tra cơ chế hiển thị tên (BidderDisplay)
        assertEquals("User U001", history.get(0).getBidderName(), "Phải có tiền tố 'User ' nếu có ID");
        assertEquals(2600.0, history.get(0).getAmount());

        assertEquals("Anonymous", history.get(1).getBidderName(), "Phải đổi thành 'Anonymous' nếu ID là null");
        assertEquals(2700.0, history.get(1).getAmount());
    }
}