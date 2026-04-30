package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.dao.AuctionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Base class cho các handler xử lý action từ client.
 * Cung cấp danh sách action hỗ trợ và helper broadcast/parse dùng chung.
 */
public abstract class AbstractClientActionHandler implements ClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientActionHandler.class);

    // Mỗi handler tự khai báo nhóm action mà nó chịu trách nhiệm xử lý.
    private final Set<String> supportedActions;

    protected AbstractClientActionHandler(String... supportedActions) {
        this.supportedActions = Set.of(supportedActions);
    }

    @Override
    public boolean canHandle(String action) {
        return action != null && supportedActions.contains(action);
    }

    protected void broadcastRoomList(ClientActionContext context) {
        try {
            // Sau khi tạo/xóa/duyệt phòng, mọi lobby cần nhận danh sách phòng mới nhất.
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> rooms = auctionDAO.getAllActiveAuctions();
            context.broadcastAll(new Message("ROOM_LIST", "SERVER", rooms));
        } catch (Exception e) {
            LOGGER.error("Failed to broadcast room list.", e);
        }
    }

    protected void broadcastPendingAuctionList(ClientActionContext context) {
        try {
            // Admin dashboard cần cập nhật danh sách yêu cầu chờ duyệt theo thời gian thực.
            context.broadcastAll(new Message(
                    "ADMIN_PENDING_AUCTION_LIST",
                    "SERVER",
                    context.getPendingAuctionApprovalService().getAllPending()
            ));
        } catch (Exception e) {
            LOGGER.error("Failed to broadcast pending auction list.", e);
        }
    }

    protected double parseBidAmount(Object data) {
        // Client có thể gửi số dưới nhiều kiểu object, chuẩn hóa về double trước khi xử lý.
        if (data instanceof Double d) {
            return d;
        }
        if (data instanceof Integer i) {
            return i.doubleValue();
        }
        if (data instanceof Long l) {
            return l.doubleValue();
        }
        return Double.parseDouble(data.toString().trim());
    }

    protected String generateId(String prefix, int digits) {
        // Sinh id ngắn theo thời gian, đủ dùng cho request tạm trong bộ nhớ.
        long modulo = 1L;
        for (int i = 0; i < digits; i++) {
            modulo *= 10;
        }

        long value = System.currentTimeMillis() % modulo;
        return prefix + String.format("%0" + digits + "d", value);
    }
}
