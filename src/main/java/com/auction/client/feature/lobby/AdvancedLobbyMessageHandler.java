package com.auction.client.feature.lobby;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;

import java.util.Collections;
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
        return "ROOM_LIST".equals(action) || "UPDATE_PRICE".equals(action);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Message message) {
        if ("UPDATE_PRICE".equals(message.getAction())) {
            handleUpdatePrice(message);
            return;
        }

        Object data = message.getData();
        List<AuctionRoom> rooms;

        if (data == null) {
            rooms = Collections.emptyList();
        } else if (data instanceof String raw) {
            rooms = rawMapper.map(raw);
        } else if (data instanceof List<?> rawList) {
            if (rawList.isEmpty()) {
                rooms = Collections.emptyList();
            } else if (rawList.get(0) instanceof AuctionRoom) {
                rooms = (List<AuctionRoom>) rawList;
            } else {
                System.err.println("[AdvancedLobbyMessageHandler] ROOM_LIST contains a non-AuctionRoom item: "
                        + rawList.get(0).getClass().getName());
                rooms = Collections.emptyList();
            }
        } else {
            System.err.println("[AdvancedLobbyMessageHandler] Unsupported ROOM_LIST data type: "
                    + data.getClass().getName());
            rooms = Collections.emptyList();
        }

        List<LobbyRoomDisplayModel> models = displayMapper.map(rooms);
        renderer.render(models);
    }

    private void handleUpdatePrice(Message message) {
        Object data = message.getData();
        if (data == null) return;

        try {
            String[] parts = data.toString().split("\\|");
            if (parts.length < 2) return;

            String roomId = parts[0].trim();
            double newPrice = Double.parseDouble(parts[1].trim());

            renderer.updatePrice(roomId, newPrice);
        } catch (Exception e) {
            System.err.println("[AdvancedLobbyMessageHandler] Failed to handle UPDATE_PRICE: " + e.getMessage());
        }
    }
}
