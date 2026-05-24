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
import com.auction.server.service.AutoBidManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class RoomActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomActionHandler.class);

    // KHAI BÁO Ở ĐÂY ĐỂ TẤT CẢ CÁC HÀM BÊN DƯỚI ĐỀU NHÌN THẤY
    private static final AutoBidManager autoBidManager = new AutoBidManager();

    public RoomActionHandler() {
        super(
                "JOIN_ROOM",
                "LEAVE_ROOM",
                "GET_ROOMS",
                "BID",
                "CHAT_MSG",
                "GET_BID_HISTORY",
                "GET_PRODUCT_DETAILS",
                "CLOSE_AUCTION",
                "SET_AUTO_BID",
                "CANCEL_AUTO_BID"
        );
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        switch (message.getAction()) {
            case "JOIN_ROOM" -> handleJoinRoom(message, context);
            case "LEAVE_ROOM" -> context.clearCurrentRoom();
            case "GET_ROOMS" -> handleGetRooms(context);
            case "BID" -> handleBid(message, context);
            case "CHAT_MSG" -> handleChat(message, context);
            case "GET_BID_HISTORY" -> handleGetBidHistory(message, context);
            case "GET_PRODUCT_DETAILS" -> handleGetProductDetails(message, context);
            case "CLOSE_AUCTION" -> handleCloseAuction(message, context);
            case "SET_AUTO_BID" -> handleSetAutoBid(message, context);
            case "CANCEL_AUTO_BID" -> handleCancelAutoBid(message, context);
            default ->
                    context.send(new Message("UNKNOWN_ACTION", "SERVER", "Unsupported action: " + message.getAction()));
        }
    }

    // ==========================================================
    // CÁC HÀM XỬ LÝ AUTO-BID MỚI
    // ==========================================================
    private void handleSetAutoBid(Message message, ClientActionContext context) {
        try {
            com.auction.common.dto.AutoBidRequest request = (com.auction.common.dto.AutoBidRequest) message.getData();

            // 1. CHỈ CẦN TRUYỀN ID THẬT CỦA USER VÀO ĐÂY (Bỏ vụ tra cứu Username đi cho gọn)
            autoBidManager.registerAutoBid(request.getRoomId(), context.getUserId(), request.getMaxBid(), request.getIncrement());

            LOGGER.info("User {} set Auto-Bid: Max {}, Step {}", context.getUserId(), request.getMaxBid(), request.getIncrement());

            // 2. Kích hoạt Robot ngay lập tức
            AuctionRoom room = context.getRoomService().getLiveRoom(request.getRoomId());
            if (room != null) {
                autoBidManager.triggerAutoBids(request.getRoomId(), room, context);
            }

        } catch (Exception e) {
            LOGGER.error("Set Auto-Bid processing error.", e);
        }
    }

    private void handleCancelAutoBid(Message message, ClientActionContext context) {
        try {
            String roomId = message.getData().toString();

            // 1. Phải tra cứu lại đúng cái tên hiển thị (Username) đã dùng để đăng ký ban nãy
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserById(context.getUserId());
            String realUsername = (user != null && user.getUsername() != null)
                    ? user.getUsername()
                    : context.getUserId();

            // 2. Đưa đúng tên thật vào để tìm và gỡ Robot khỏi hàng đợi
            autoBidManager.cancelAutoBid(roomId, realUsername);

            LOGGER.info("User {} ({}) canceled Auto-Bid in room {}", context.getUserId(), realUsername, roomId);
        } catch (Exception e) {
            LOGGER.error("Cancel Auto-Bid processing error.", e);
        }
    }
    // ==========================================================
    // CÁC HÀM CŨ
    // ==========================================================
    private void handleBid(Message message, ClientActionContext context) {
        try {
            if (context.getCurrentRoomId().isBlank()) {
                context.send(new Message("BID_FAIL", "SERVER", "You have not joined any room!"));
                return;
            }

            double bidAmount = parseBidAmount(message.getData());
            Message bidResult = context.getRoomService().placeNewBid(
                    context.getCurrentRoomId(),
                    context.getUserId(),
                    bidAmount
            );

            if ("BID_SUCCESS".equals(bidResult.getAction()) || "BID_SUCCESS_EXTENDED".equals(bidResult.getAction())) {
                context.broadcastToRoom(context.getCurrentRoomId(), bidResult);
                String updatePayload = context.getCurrentRoomId() + "|" + bidAmount;
                context.broadcastAll(new Message("UPDATE_PRICE", "SERVER", updatePayload));

                if (bidResult.getData() instanceof AuctionRoom) {
                    AuctionRoom updatedRoom = (AuctionRoom) bidResult.getData();
                    autoBidManager.triggerAutoBids(updatedRoom.getRoomId(), updatedRoom, context);
                }
            } else {
                context.send(bidResult);
            }
        } catch (Exception e) {
            LOGGER.error("Bid processing error.", e);
            context.send(new Message("BID_FAIL", "SERVER", "Bid processing error!"));
        }
    }

    private void handleJoinRoom(Message message, ClientActionContext context) {
        try {
            String roomId = message.getData() != null ? message.getData().toString().trim() : "";
            if (roomId.isEmpty()) {
                context.send(new Message("ROOM_FAIL", "SERVER", "Missing auction room ID!"));
                return;
            }

            context.setCurrentRoomId(roomId);
            Message joinResult = context.getRoomService().joinRoom(context.getCurrentRoomId(), context.getUserId());

            if ("ROOM_FAIL".equals(joinResult.getAction())) {
                context.clearCurrentRoom();
            }

            context.send(joinResult);
            if ("ROOM_JOINED".equals(joinResult.getAction())) {
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
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> allRooms = auctionDAO.getAllActiveAuctions();
            context.send(new Message("ROOM_LIST", "SERVER", allRooms));
        } catch (Exception e) {
            LOGGER.error("Unable to load rooms.", e);
            context.send(new Message("ROOM_LIST", "SERVER", Collections.emptyList()));
        }
    }

    private void handleChat(Message message, ClientActionContext context) {
        try {
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
            boolean closed = auctionDAO.closeAuctionBySeller(roomId, context.getUserId());
            if (!closed) {
                context.send(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Unable to close this auction!"));
                return;
            }

            AuctionStateManager.removeState(roomId);
            AuctionImageRegistry.remove(roomId);
            context.send(new Message("CLOSE_AUCTION_SUCCESS", "SERVER", roomId));
            context.notifyRoomClosed(roomId);
            broadcastRoomList(context);
        } catch (Exception e) {
            LOGGER.error("Unable to close auction.", e);
            context.send(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Unable to close auction!"));
        }
    }
}
