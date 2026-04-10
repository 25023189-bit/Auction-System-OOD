package com.auction.client.room;

import com.auction.client.messaging.MessageHandler;
import com.auction.client.navigation.SceneNavigator;
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
            case "ROOM_JOINED", "CHAT_MSG", "UPDATE_PRICE" -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Message message) {
        switch (message.getAction()) {
            case "ROOM_JOINED" -> {
                AuctionRoom room = (AuctionRoom) message.getData();
                sessionStore.setCurrentRoom(room);
                sessionStore.setCurrentRoomId(room != null ? room.getRoomId() : null);
                sceneNavigator.showAuctionRoom(room);
                binder.bind(room);
                if (room != null) {
                    auctionTimer.start(room.getStartTime(), room.getActualEndTime());
                }
            }
            case "CHAT_MSG" -> presenter.appendChat("[" + message.username + "]: " + message.data);
            case "UPDATE_PRICE" -> {
                String[] parts = ((String) message.data).split("\\|");
                String currentRoomId = sessionStore.getCurrentRoomId();
                if (currentRoomId != null && currentRoomId.equals(parts[0])) {
                    double newPrice = Double.parseDouble(parts[1]);
                    presenter.showCurrentPrice(newPrice, parts[2]);
                }
            }
        }
    }
}