package com.auction.client.feature.room;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageHandler xử lý các phản hồi liên quan trực tiếp tới phòng đấu giá đang mở.
 *
 * Vai trò:
 * - Đồng bộ trạng thái room, giá, chat và timer khi server gửi event phòng.
 * - Lưu room hiện tại vào session và bind dữ liệu lên presenter.
 *
 * Luồng chính:
 * 1. ResponseRouter chuyển ROOM_JOINED, ROOM_STATE_UPDATED, BID_SUCCESS, CHAT_MSG hoặc UPDATE_PRICE vào handler.
 * 2. Handler lọc đúng currentRoomId, cập nhật SessionStore, binder, presenter và AuctionTimer.
 *
 * Business rules:
 * - Chỉ cập nhật UI nếu message thuộc đúng phòng hiện tại.
 * - BID_SUCCESS_EXTENDED phải thông báo việc gia hạn phiên cho người trong phòng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: cập nhật presenter/session/timer mutable trên luồng UI.
 * - Dependency: MessageHandler, SessionStore, SceneNavigator, AuctionRoomStateBinder, AuctionRoomPresenter, AuctionTimer.
 */
public class AuctionRoomMessageHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionRoomMessageHandler.class);

    private final SessionStore sessionStore;
    private final SceneNavigator sceneNavigator;
    private final AuctionRoomStateBinder binder;
    private final AuctionRoomPresenter presenter;
    private final AuctionTimer auctionTimer;

    public AuctionRoomMessageHandler(SessionStore sessionStore,
                                     SceneNavigator sceneNavigator,
                                     AuctionRoomStateBinder binder,
                                     AuctionRoomPresenter presenter,
                                     AuctionTimer auctionTimer) {
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.binder = binder;
        this.presenter = presenter;
        this.auctionTimer = auctionTimer;
    }

    @Override
    public boolean supports(String action) {
        // Chỉ nhận các action làm thay đổi trạng thái phòng, giá hoặc chat.
        return switch (action) {
            case "ROOM_STATE_UPDATED",
                 "BID_SUCCESS",
                 "BID_SUCCESS_EXTENDED",
                 "CHAT_MSG",
                 "UPDATE_PRICE" -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Message message) {
        switch (message.getAction()) {
            case "ROOM_STATE_UPDATED" -> handleRoomStateUpdated(message);
            case "BID_SUCCESS", "BID_SUCCESS_EXTENDED" -> handleBidSuccess(message);
            case "CHAT_MSG" -> presenter.appendChat("[" + message.username + "]: " + message.data);
            case "UPDATE_PRICE" -> handleUpdatePrice(message);
        }
    }

    // Đồng bộ lại toàn bộ trạng thái phòng khi server broadcast cập nhật.
    private void handleRoomStateUpdated(Message message) {
        if (!(message.getData() instanceof AuctionRoom room)) return;

        String currentRoomId = sessionStore.getCurrentRoomId();
        if (currentRoomId == null || !currentRoomId.equals(room.getRoomId())) return;

        sessionStore.setCurrentRoom(room);
        binder.bind(room);

        presenter.showCurrentPrice(room.getCurrentPrice(), room.getHighestBidder());

        if (room.getStartTime() != null) {
            auctionTimer.start(
                    room.getStartTime(),
                    room.getScheduledEndTime() != null ? room.getScheduledEndTime() : room.getEndTime()
            );
        }
    }

    // Cập nhật UI sau bid thành công, bao gồm cả trường hợp phiên được gia hạn.
    private void handleBidSuccess(Message message) {
        if (!(message.getData() instanceof AuctionRoom room)) {
            presenter.appendChat("New bid received!");
            return;
        }

        String currentRoomId = sessionStore.getCurrentRoomId();
        if (currentRoomId == null || !currentRoomId.equals(room.getRoomId())) return;

        sessionStore.setCurrentRoom(room);
        binder.bind(room);
        presenter.showCurrentPrice(room.getCurrentPrice(), room.getHighestBidder());

        if ("BID_SUCCESS_EXTENDED".equals(message.getAction())) {
            presenter.appendChat("Auction extended because a bid was placed in the final 30 seconds.");
        }

        if (room.getStartTime() != null) {
            auctionTimer.start(
                    room.getStartTime(),
                    room.getScheduledEndTime() != null ? room.getScheduledEndTime() : room.getEndTime()
            );
        }
    }

    // UPDATE_PRICE có dạng "roomId|price" để cập nhật nhanh giá hiện tại.
    private void handleUpdatePrice(Message message) {
        Object data = message.getData();
        if (data == null) return;

        try {
            String[] parts = data.toString().split("\\|");
            if (parts.length < 2) return;

            String roomId = parts[0].trim();
            double newPrice = Double.parseDouble(parts[1].trim());

            String currentRoomId = sessionStore.getCurrentRoomId();
            if (currentRoomId == null || !currentRoomId.equals(roomId)) return;

            AuctionRoom currentRoom = sessionStore.getCurrentRoom();
            if (currentRoom != null) {
                currentRoom.setCurrentPrice(newPrice);
            }

            presenter.showCurrentPrice(newPrice, null);

        } catch (Exception e) {
            LOGGER.error("Failed to handle UPDATE_PRICE.", e);
        }
    }
}
