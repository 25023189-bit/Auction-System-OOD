package com.auction.server.service;

import com.auction.common.model.PendingAuctionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PendingAuctionApprovalServiceTest {

    private PendingAuctionApprovalService service;

    @BeforeEach
    void setUp() {
        service = new PendingAuctionApprovalService();
        // Dọn dẹp RAM sạch sẽ trước mỗi bài test
        clearPendingQueue();
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp RAM sạch sẽ sau khi test xong để không ảnh hưởng bài khác
        clearPendingQueue();
    }

    // Hàm helper tự động lấy tất cả các request đang kẹt và xóa chúng đi
    private void clearPendingQueue() {
        List<PendingAuctionRequest> pending = service.getAllPending();
        for (PendingAuctionRequest req : pending) {
            service.reject(req.getRequestId());
        }
    }

    // ==========================================
    // TEST NGHIỆP VỤ SUBMIT (THÊM YÊU CẦU)
    // ==========================================

    @Test
    @DisplayName("Submit: Bỏ qua và không lưu nếu request null hoặc Request ID trống")
    void submit_NullOrBlankRequest_Ignores() {
        // Trường hợp 1: Request truyền vào là null
        service.submit(null);

        // Trường hợp 2: Request ID bị null
        PendingAuctionRequest nullIdRequest = mock(PendingAuctionRequest.class);
        when(nullIdRequest.getRequestId()).thenReturn(null);
        service.submit(nullIdRequest);

        // Trường hợp 3: Request ID là khoảng trắng
        PendingAuctionRequest blankIdRequest = mock(PendingAuctionRequest.class);
        when(blankIdRequest.getRequestId()).thenReturn("   ");
        service.submit(blankIdRequest);

        // Đảm bảo không có request rác nào lọt được vào hàng chờ
        assertTrue(service.getAllPending().isEmpty(), "Hàng chờ phải trống trơn");
    }

    @Test
    @DisplayName("Submit: Lưu thành công request hợp lệ vào hệ thống")
    void submit_ValidRequest_AddsToQueue() {
        PendingAuctionRequest validRequest = mock(PendingAuctionRequest.class);
        when(validRequest.getRequestId()).thenReturn("REQ-001");

        service.submit(validRequest);

        List<PendingAuctionRequest> pending = service.getAllPending();
        assertEquals(1, pending.size(), "Phải có đúng 1 request trong hàng chờ");
        assertEquals("REQ-001", pending.get(0).getRequestId());
    }

    // ==========================================
    // TEST NGHIỆP VỤ GET ALL (LẤY DANH SÁCH)
    // ==========================================

    @Test
    @DisplayName("GetAllPending: Lấy đúng toàn bộ danh sách đang chờ duyệt")
    void getAllPending_ReturnsAllRequests() {
        PendingAuctionRequest req1 = mock(PendingAuctionRequest.class);
        when(req1.getRequestId()).thenReturn("REQ-001");

        PendingAuctionRequest req2 = mock(PendingAuctionRequest.class);
        when(req2.getRequestId()).thenReturn("REQ-002");

        service.submit(req1);
        service.submit(req2);

        List<PendingAuctionRequest> pending = service.getAllPending();

        assertEquals(2, pending.size(), "Phải lấy ra được đúng 2 request");
        assertTrue(pending.contains(req1));
        assertTrue(pending.contains(req2));
    }

    // ==========================================
    // TEST NGHIỆP VỤ APPROVE (DUYỆT YÊU CẦU)
    // ==========================================

    @Test
    @DisplayName("Approve: Trả về null nếu ID null, khoảng trắng hoặc không tồn tại")
    void approve_InvalidOrNonExistentId_ReturnsNull() {
        assertNull(service.approve(null));
        assertNull(service.approve("   "));
        assertNull(service.approve("FAKE-REQ"));
    }

    @Test
    @DisplayName("Approve: Duyệt thành công, trả về Request và XÓA khỏi hàng chờ")
    void approve_ValidId_ReturnsAndRemovesFromQueue() {
        PendingAuctionRequest req = mock(PendingAuctionRequest.class);
        when(req.getRequestId()).thenReturn("REQ-001");
        service.submit(req);

        // Tiến hành Approve
        PendingAuctionRequest approvedReq = service.approve("REQ-001");

        // Đảm bảo trả về đúng đối tượng
        assertNotNull(approvedReq);
        assertEquals("REQ-001", approvedReq.getRequestId());

        // Đảm bảo nó đã bị xóa khỏi hàng chờ
        assertTrue(service.getAllPending().isEmpty(), "Sau khi approve thì hàng chờ phải trống");
    }

    // ==========================================
    // TEST NGHIỆP VỤ REJECT (TỪ CHỐI YÊU CẦU)
    // ==========================================

    @Test
    @DisplayName("Reject: Trả về false nếu ID null, khoảng trắng hoặc không tồn tại")
    void reject_InvalidOrNonExistentId_ReturnsFalse() {
        assertFalse(service.reject(null));
        assertFalse(service.reject("   "));
        assertFalse(service.reject("FAKE-REQ"));
    }

    @Test
    @DisplayName("Reject: Từ chối thành công, trả về true và XÓA khỏi hàng chờ")
    void reject_ValidId_ReturnsTrueAndRemovesFromQueue() {
        PendingAuctionRequest req = mock(PendingAuctionRequest.class);
        when(req.getRequestId()).thenReturn("REQ-001");
        service.submit(req);

        // Từ chối sẽ có trim() khoảng trắng dư thừa
        boolean isRejected = service.reject("  REQ-001  ");

        assertTrue(isRejected, "Phải trả về true khi xóa thành công");
        assertTrue(service.getAllPending().isEmpty(), "Sau khi reject thì hàng chờ phải trống");
    }
}