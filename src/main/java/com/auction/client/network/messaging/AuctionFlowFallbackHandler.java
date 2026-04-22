package com.auction.client.network.messaging;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
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
                 "CREATE_AUCTION_FAIL",
                 "UPDATE_ROOMS",
                 "BID_FAIL",
                 "ROOM_FAIL",
                 "BID_SUCCESS",
                 "CLOSE_AUCTION_SUCCESS",
                 "CLOSE_AUCTION_FAIL",
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
            case "CREATE_AUCTION_FAIL" -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, String.valueOf(msg.getData()));
                alert.setHeaderText("Unable to Create Auction");
                alert.showAndWait();
            }
            case "UPDATE_ROOMS" -> auctionService.getRooms();
            case "BID_FAIL" -> {
                if (auctionRoomPresenter != null) {
                    auctionRoomPresenter.appendChat("Error: " + msg.getData());
                }
            }
            case "ROOM_FAIL" -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, String.valueOf(msg.getData()));
                alert.setHeaderText("Unable to Join Room");
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
            case "CLOSE_AUCTION_SUCCESS" -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The auction was closed by the seller.");
                alert.setHeaderText("Auction Closed");
                alert.showAndWait();
            }
            case "CLOSE_AUCTION_FAIL" -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, String.valueOf(msg.getData()));
                alert.setHeaderText("Unable to Close Auction");
                alert.showAndWait();
            }
            case "AUCTION_CLOSED_NOTIFY" -> {
                String currentRoomId = sessionStore.getCurrentRoomId();
                if (currentRoomId != null && currentRoomId.equals(String.valueOf(msg.getData()))) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "The auction has ended. You will be returned to the main lobby.");
                    alert.setHeaderText("Auction Ended");
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
