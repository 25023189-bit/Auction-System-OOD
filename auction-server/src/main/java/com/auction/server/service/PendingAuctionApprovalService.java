package com.auction.server.service;

import com.auction.common.model.PendingAuctionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service lưu và xử lý yêu cầu tạo phiên đấu giá đang chờ admin duyệt.
 *
 * Vai trò:
 * - Nhận PendingAuctionRequest từ seller sau khi validate.
 * - Cung cấp danh sách pending, approve hoặc reject request cho admin.
 *
 * Luồng chính:
 * 1. SellerActionHandler submit request vào map pending dùng chung toàn server.
 * 2. AdminActionHandler lấy danh sách pending, approve để remove và tạo auction thật hoặc reject để xóa.
 *
 * Business rules:
 * - Request không có requestId hợp lệ sẽ bị bỏ qua.
 * - Approve/reject đều remove request khỏi hàng chờ; nếu approve lưu DB lỗi thì handler có thể submit lại.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe ở cấp map nhờ ConcurrentHashMap; thứ tự getAllPending() không được đảm bảo.
 * - Dependency: PendingAuctionRequest, ConcurrentHashMap, List/ArrayList.
 */
public class PendingAuctionApprovalService {
    // Static map để mọi ClientHandler nhìn thấy cùng một danh sách pending.
    private static final Map<String, PendingAuctionRequest> PENDING_REQUESTS = new ConcurrentHashMap<>();

    public void submit(PendingAuctionRequest request) {
        if (request == null || request.getRequestId() == null || request.getRequestId().isBlank()) {
            return;
        }
        PENDING_REQUESTS.put(request.getRequestId(), request);
    }

    public List<PendingAuctionRequest> getAllPending() {
        return new ArrayList<>(PENDING_REQUESTS.values());
    }

    public PendingAuctionRequest approve(String requestId) {
        // Approve lấy request ra khỏi hàng chờ để AdminActionHandler tạo auction thật.
        if (requestId == null || requestId.isBlank()) {
            return null;
        }
        return PENDING_REQUESTS.remove(requestId.trim());
    }

    public boolean reject(String requestId) {
        // Reject chỉ cần xóa request khỏi hàng chờ.
        if (requestId == null || requestId.isBlank()) {
            return false;
        }
        return PENDING_REQUESTS.remove(requestId.trim()) != null;
    }
}
