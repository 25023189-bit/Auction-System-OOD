package com.auction.client.network.messaging;

import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.room.AuctionRoomPresenter;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.server.service.AuctionService;
import javafx.scene.control.Alert;

public class AuctionFlowFallbackHandler implements MessageHandler {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;
    private final SceneNavigator sceneNavigator;
    private final LobbyUserInfoBinder lobbyUserInfoBinder;
    private final AuctionRoomPresenter auctionRoomPresenter;

    public AuctionFlowFallbackHandler(AuctionService auctionService,
                                      SessionStore sessionStore,
                                      SceneNavigator sceneNavigator,
                                      LobbyUserInfoBinder lobbyUserInfoBinder,
                                      AuctionRoomPresenter auctionRoomPresenter) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.lobbyUserInfoBinder = lobbyUserInfoBinder;
        this.auctionRoomPresenter = auctionRoomPresenter;
    }

    @Override
    public boolean supports(String action) {
        return switch (action) {
            case "CREATE_AUCTION_SUCCESS",
                 "UPDATE_ROOMS",
                 "BID_FAIL",
                 "ROOM_FAIL",
                 "BID_SUCCESS",
                 "AUCTION_CLOSED_NOTIFY" -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Message msg) {
        switch (msg.getAction()) {
            case "CREATE_AUCTION_SUCCESS" -> {
                sessionStore.setCurrentRoomId(msg.id);
                auctionService.joinRoom(msg.id);
            }

            case "UPDATE_ROOMS" -> auctionService.getRooms();

            case "BID_FAIL" -> {
                if (auctionRoomPresenter != null) {
                    auctionRoomPresenter.appendChat("❌ " + msg.getData());
                }
            }

            case "ROOM_FAIL" -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, String.valueOf(msg.getData()));
                alert.setHeaderText("Không thể vào phòng");
                alert.showAndWait();

                sessionStore.setCurrentRoom(null);
                sessionStore.setCurrentRoomId(null);

                sceneNavigator.showLobby();
                if (lobbyUserInfoBinder != null) {
                    lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
                }
                auctionService.getRooms();
            }

            case "BID_SUCCESS" -> {
                if (auctionRoomPresenter != null && msg.getData() instanceof Double price) {
                    auctionRoomPresenter.showCurrentPrice(price, null);
                }
            }

            case "AUCTION_CLOSED_NOTIFY" -> {
                String currentRoomId = sessionStore.getCurrentRoomId();
                if (currentRoomId != null && currentRoomId.equals(String.valueOf(msg.getData()))) {
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "Phiên đấu giá đã kết thúc. Bạn sẽ được đưa về sảnh chính.");
                    alert.setHeaderText("Phiên đấu giá kết thúc");
                    alert.showAndWait();

                    sessionStore.setCurrentRoom(null);
                    sessionStore.setCurrentRoomId(null);

                    sceneNavigator.showLobby();
                    if (lobbyUserInfoBinder != null) {
                        lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
                    }
                    auctionService.getRooms();
                }
            }
        }
    }
}