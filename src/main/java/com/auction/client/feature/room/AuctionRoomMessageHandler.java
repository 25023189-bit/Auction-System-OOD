package com.auction.client.feature.room;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;

public class AuctionRoomMessageHandler implements MessageHandler {
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
        return switch (action) {
            case "ROOM_JOINED",
                 "ROOM_STATE_UPDATED",
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
            case "ROOM_JOINED" -> handleRoomJoined(message);
            case "ROOM_STATE_UPDATED" -> handleRoomStateUpdated(message);
            case "BID_SUCCESS", "BID_SUCCESS_EXTENDED" -> handleBidSuccess(message);
            case "CHAT_MSG" -> presenter.appendChat("[" + message.username + "]: " + message.data);
            case "UPDATE_PRICE" -> handleUpdatePrice(message);
        }
    }

    private void handleRoomJoined(Message message) {
        if (!(message.getData() instanceof AuctionRoom room)) return;

        sessionStore.setCurrentRoom(room);
        sessionStore.setCurrentRoomId(room.getRoomId());

        sceneNavigator.showAuctionRoom(room);
        binder.bind(room);

        if (room.getStartTime() != null) {
            auctionTimer.start(
                    room.getStartTime(),
                    room.getScheduledEndTime() != null ? room.getScheduledEndTime() : room.getEndTime()
            );
        }
    }

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
            System.err.println("[AuctionRoomMessageHandler] Failed to handle UPDATE_PRICE: " + e.getMessage());
        }
    }
}
