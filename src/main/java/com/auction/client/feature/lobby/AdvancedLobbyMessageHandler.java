package com.auction.client.feature.lobby;

import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;

import java.util.List;

public class AdvancedLobbyMessageHandler implements MessageHandler {
    private final DisplayMapper<String, List<AuctionRoom>> rawMapper;
    private final DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> displayMapper;
    private final LobbyRoomListRenderer renderer;

    public AdvancedLobbyMessageHandler(DisplayMapper<String, List<AuctionRoom>> rawMapper,
                                       DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> displayMapper,
                                       LobbyRoomListRenderer renderer) {
        this.rawMapper = rawMapper;
        this.displayMapper = displayMapper;
        this.renderer = renderer;
    }

    @Override
    public boolean supports(String action) {
        return "ROOM_LIST".equals(action);
    }

    @Override
    public void handle(Message message) {
        List<AuctionRoom> rooms = rawMapper.map((String) message.getData());
        List<LobbyRoomDisplayModel> models = displayMapper.map(rooms);
        renderer.render(models);
    }
}