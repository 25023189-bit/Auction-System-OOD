package com.auction.client.feature.controllers.auction.lobby;

import com.auction.client.feature.lobby.AdvancedLobbyMessageHandler;
import com.auction.client.feature.lobby.DefaultAuctionCardFactory;
import com.auction.client.feature.lobby.LobbyRoomListRenderer;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.client.shared.mapper.RoomDisplayMapper;
import com.auction.client.shared.mapper.RoomListMapper;
import com.auction.common.model.AuctionRoom;
import com.auction.common.role.RolePolicy;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class LobbyViewCoordinator {
    private final LobbyUserInfoBinder lobbyUserInfoBinder;
    private final MessageHandler lobbyMessageHandler;

    public LobbyViewCoordinator(
            Label lblUsername,
            Label lblBalance,
            Button btnCreateAuction,
            FlowPane paneSelectAuction,
            RolePolicy rolePolicy,
            AuctionService auctionService,
            SessionStore sessionStore
    ) {
        lobbyUserInfoBinder = new LobbyUserInfoBinder(lblUsername, lblBalance, btnCreateAuction, rolePolicy);
        DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> roomDisplayMapper = new RoomDisplayMapper();
        LobbyRoomListRenderer roomListRenderer = new LobbyRoomListRenderer(
                paneSelectAuction,
                new DefaultAuctionCardFactory(auctionService)
        );
        lobbyMessageHandler = new AdvancedLobbyMessageHandler(new RoomListMapper(), roomDisplayMapper, roomListRenderer);

        if (btnCreateAuction != null) {
            btnCreateAuction.setVisible(false);
            btnCreateAuction.setManaged(false);
        }
        if (sessionStore != null && sessionStore.getCurrentUser() != null) {
            lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
        }
    }

    public MessageHandler lobbyMessageHandler() {
        return lobbyMessageHandler;
    }

    public LobbyUserInfoBinder lobbyUserInfoBinder() {
        return lobbyUserInfoBinder;
    }
}
