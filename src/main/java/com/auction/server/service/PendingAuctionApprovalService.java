package com.auction.server.service;

import com.auction.common.model.PendingAuctionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lưu các yêu cầu tạo phiên đấu giá đang chờ admin duyệt.
 * Hiện dùng bộ nhớ server, chưa persist xuống database.
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
