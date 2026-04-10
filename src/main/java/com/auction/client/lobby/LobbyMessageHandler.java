package com.auction.client.lobby;

import com.auction.client.mapper.DisplayMapper;
import com.auction.client.messaging.MessageHandler;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;

import java.util.List;

public class LobbyMessageHandler implements MessageHandler {
    private final LobbyPresenter presenter;
    private final DisplayMapper<String, List<AuctionRoom>> mapper;

    public LobbyMessageHandler(LobbyPresenter presenter,
                               DisplayMapper<String, List<AuctionRoom>> mapper) {
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @Override
    public boolean supports(String action) {
        return "ROOM_LIST".equals(action);
    }

    @Override
    public void handle(Message message) {
        String rawData = (String) message.getData();
        List<AuctionRoom> rooms = mapper.map(rawData);
        presenter.showRooms(rooms);
    }
}