package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.ProductDetailResponse;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuctionStateManager;
import com.auction.server.service.ProductDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Handler xử lý các action trong lobby và phòng đấu giá.
 *
 * Vai trò:
 * - Điều phối join/leave room, lấy danh sách room, đặt giá, chat và xem lịch sử bid.
 * - Gọi service/DAO để lấy chi tiết sản phẩm và đóng phiên theo yêu cầu seller.
 *
 * Luồng chính:
 * 1. Nhận Message room-related, switch theo action và parse roomId/bid/payload.
 * 2. Gọi AuctionRoomService hoặc DAO/service phù hợp rồi gửi response/broadcast cho client liên quan.
 *
 * Business rules:
 * - Client phải join room thành công trước khi được bid trong room đó.
 * - Bid thành công phải broadcast state trong phòng và cập nhật giá cho lobby.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe theo instance; trạng thái room nằm trong ClientActionContext và AuctionStateManager.
 * - Dependency: AbstractClientActionHandler, AuctionRoomService, AuctionDAO, TransactionDAO, ProductDetailService, AuctionStateManager.
 */
public class RoomActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomActionHandler.class);

    public RoomActionHandler() {
        super(
                "JOIN_ROOM",
                "LEAVE_ROOM",
                "GET_ROOMS",
                "BID",
                "CHAT_MSG",
                "GET_BID_HISTORY",
                "GET_PRODUCT_DETAILS",
                "CLOSE_AUCTION"
        );
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        // Các action phòng đấu giá được gom ở đây để dùng chung currentRoomId trong context.
        switch (message.getAction()) {
            case "JOIN_ROOM" -> handleJoinRoom(message, context);
            case "LEAVE_ROOM" -> context.clearCurrentRoom();
            case "GET_ROOMS" -> handleGetRooms(context);
            case "BID" -> handleBid(message, context);
            case "CHAT_MSG" -> handleChat(message, context);
            case "GET_BID_HISTORY" -> handleGetBidHistory(message, context);
            case "GET_PRODUCT_DETAILS" -> handleGetProductDetails(message, context);
            case "CLOSE_AUCTION" -> handleCloseAuction(message, context);
            default -> context.send(new Message("UNKNOWN_ACTION", "SERVER", "Unsupported action: " + message.getAction()));
        }
    }

    private void handleJoinRoom(Message message, ClientActionContext context) {
        try {
            String roomId = message.getData() != null ? message.getData().toString().trim() : "";
            if (roomId.isEmpty()) {
                context.send(new Message("ROOM_FAIL", "SERVER", "Missing auction room ID!"));
                return;
            }

            // Lưu tạm roomId trước khi gọi service; nếu join fail sẽ clear lại ngay bên dưới.
            context.setCurrentRoomId(roomId);
            Message joinResult = context.getRoomService().joinRoom(context.getCurrentRoomId(), context.getUserId());

            if ("ROOM_FAIL".equals(joinResult.getAction())) {
                context.clearCurrentRoom();
            }

            context.send(joinResult);
            if ("ROOM_JOINED".equals(joinResult.getAction())) {
                // Broadcast trạng thái phòng cho những client đã ở cùng phòng.
                context.broadcastToRoom(
                        context.getCurrentRoomId(),
                        new Message("ROOM_STATE_UPDATED", "SERVER", joinResult.getData())
                );
            }
        } catch (Exception e) {
            LOGGER.error("Join room processing error.", e);
            context.clearCurrentRoom();
            context.send(new Message("ROOM_FAIL", "SERVER", "Join room processing error!"));
        }
    }

    private void handleGetRooms(ClientActionContext context) {
        try {
            // Lobby chỉ cần danh sách phiên còn OPEN/RUNNING.
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> allRooms = auctionDAO.getAllActiveAuctions();
            context.send(new Message("ROOM_LIST", "SERVER", allRooms));
        } catch (Exception e) {
            LOGGER.error("Unable to load rooms.", e);
            context.send(new Message("ROOM_LIST", "SERVER", Collections.emptyList()));
        }
    }

    private void handleBid(Message message, ClientActionContext context) {
        try {
            if (context.getCurrentRoomId().isBlank()) {
                context.send(new Message("BID_FAIL", "SERVER", "You have not joined any room!"));
                return;
            }

            // parseBidAmount hỗ trợ Double/Integer/Long/String từ client.
            double bidAmount = parseBidAmount(message.getData());
            Message bidResult = context.getRoomService().placeNewBid(
                    context.getCurrentRoomId(),
                    context.getUserId(),
                    bidAmount
            );

            if ("BID_SUCCESS".equals(bidResult.getAction()) || "BID_SUCCESS_EXTENDED".equals(bidResult.getAction())) {
                // Người trong phòng nhận full room state; lobby chỉ cần payload roomId|price để cập nhật card.
                context.broadcastToRoom(context.getCurrentRoomId(), bidResult);
                String updatePayload = context.getCurrentRoomId() + "|" + bidAmount;
                context.broadcastAll(new Message("UPDATE_PRICE", "SERVER", updatePayload));
            } else {
                context.send(bidResult);
            }
        } catch (Exception e) {
            LOGGER.error("Bid processing error.", e);
            context.send(new Message("BID_FAIL", "SERVER", "Bid processing error!"));
        }
    }

    private void handleChat(Message message, ClientActionContext context) {
        try {
            // Lấy username thật từ DB để tin chat không phụ thuộc dữ liệu client gửi lên.
            UserDAO userDAO = new UserDAO();
            User sender = userDAO.getUserById(context.getUserId());
            String realUsername = (sender != null && sender.getUsername() != null)
                    ? sender.getUsername()
                    : "Khach";

            Message broadcastMessage = new Message("CHAT_MSG", context.getUserId(), realUsername, message.getData());
            context.broadcastToRoom(context.getCurrentRoomId(), broadcastMessage);
        } catch (Exception e) {
            LOGGER.error("Chat processing error.", e);
        }
    }

    private void handleGetBidHistory(Message message, ClientActionContext context) {
        try {
            // Lịch sử bid dùng cho dashboard admin hoặc popup chi tiết sản phẩm.
            String roomId = message.getData() != null ? message.getData().toString() : "";
            TransactionDAO transactionDAO = new TransactionDAO();
            List<BidTransaction> historyList = transactionDAO.getHistoryByRoom(roomId);
            context.send(new Message("BID_HISTORY_SUCCESS", "SERVER", historyList));
        } catch (Exception e) {
            LOGGER.error("Unable to load bid history.", e);
            context.send(new Message("BID_HISTORY_FAIL", "SERVER", "Unable to load bid history!"));
        }
    }

    private void handleGetProductDetails(Message message, ClientActionContext context) {
        try {
            String roomId = message.getData() != null ? message.getData().toString().trim() : "";
            if (roomId.isEmpty()) {
                context.send(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "Invalid room ID!"));
                return;
            }

            // Service gom thông tin sản phẩm, giá và lịch sử bid thành DTO cho popup client.
            ProductDetailService detailService = new ProductDetailService();
            ProductDetailResponse responseData = detailService.getProductDetails(roomId);

            if (responseData != null) {
                context.send(new Message("PRODUCT_DETAILS_SUCCESS", "SERVER", responseData));
            } else {
                context.send(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "Auction room details not found!"));
            }
        } catch (Exception e) {
            LOGGER.error("GET_PRODUCT_DETAILS processing error.", e);
            context.send(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "System error while loading details!"));
        }
    }

    private void handleCloseAuction(Message message, ClientActionContext context) {
        try {
            String roomId = message.getData() != null ? message.getData().toString() : "";
            if (roomId.isBlank()) {
                context.send(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Missing auction ID!"));
                return;
            }

            AuctionDAO auctionDAO = new AuctionDAO();
            // DAO kiểm tra seller hiện tại có đúng là chủ phòng trước khi đóng.
            boolean closed = auctionDAO.closeAuctionBySeller(roomId, context.getUserId());
            if (!closed) {
                context.send(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Unable to close this auction!"));
                return;
            }

            // Xóa runtime state và thông báo toàn bộ client liên quan.
            AuctionStateManager.removeState(roomId);
            context.send(new Message("CLOSE_AUCTION_SUCCESS", "SERVER", roomId));
            context.notifyRoomClosed(roomId);
            broadcastRoomList(context);
        } catch (Exception e) {
            LOGGER.error("Unable to close auction.", e);
            context.send(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Unable to close auction!"));
        }
    }
}
