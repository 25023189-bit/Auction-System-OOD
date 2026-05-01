package com.auction.client.feature.lobby;

import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;

import java.util.List;

/**
 * MessageHandler lobby đơn giản cho response ROOM_LIST dạng chuỗi legacy.
 *
 * Vai trò:
 * - Parse dữ liệu ROOM_LIST String qua DisplayMapper.
 * - Chuyển danh sách AuctionRoom đã parse cho LobbyPresenter render.
 *
 * Luồng chính:
 * 1. ResponseRouter chuyển action ROOM_LIST vào handler.
 * 2. Handler map rawData thành List<AuctionRoom> và gọi presenter.showRooms().
 *
 * Business rules:
 * - Handler này chỉ hỗ trợ ROOM_LIST, không xử lý UPDATE_PRICE.
 * - Dữ liệu message được kỳ vọng là String legacy.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: presenter JavaFX mutable, cần gọi trên JavaFX thread.
 * - Dependency: MessageHandler, LobbyPresenter, DisplayMapper<String, List<AuctionRoom>>, Message.
 */
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
