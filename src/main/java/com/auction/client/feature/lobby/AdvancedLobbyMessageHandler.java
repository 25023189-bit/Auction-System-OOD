package com.auction.client.feature.lobby;

import com.auction.client.feature.controllers.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * MessageHandler xử lý dữ liệu lobby từ server.
 *
 * Vai trò:
 * - Nhận ROOM_LIST dạng legacy String hoặc List<AuctionRoom> và chuẩn hóa dữ liệu.
 * - Xử lý UPDATE_PRICE để cập nhật nhanh giá trên card lobby.
 *
 * Luồng chính:
 * 1. ResponseRouter chuyển ROOM_LIST/UPDATE_PRICE vào handler.
 * 2. Handler map dữ liệu room sang LobbyRoomDisplayModel rồi gọi renderer render/update.
 *
 * Business rules:
 * - ROOM_LIST không hợp lệ hoặc null phải render danh sách rỗng thay vì làm lỗi UI.
 * - UPDATE_PRICE có payload roomId|price và chỉ cập nhật card tương ứng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: renderer JavaFX mutable, cần gọi trên JavaFX Application Thread.
 * - Dependency: MessageHandler, DisplayMapper, AuctionRoom, LobbyRoomDisplayModel, LobbyRoomListRenderer, SLF4J.
 */
public class AdvancedLobbyMessageHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedLobbyMessageHandler.class);

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

        // Chấp nhận nhiều định dạng để client tương thích với các kiểu response server khác nhau.
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
                LOGGER.warn("ROOM_LIST contains a non-AuctionRoom item: {}", rawList.get(0).getClass().getName());
                rooms = Collections.emptyList();
            }
        } else {
            LOGGER.warn("Unsupported ROOM_LIST data type: {}", data.getClass().getName());
            rooms = Collections.emptyList();
        }

        // Chỉ đưa dữ liệu đã chuẩn hóa sang renderer để UI không phụ thuộc model server.
        List<LobbyRoomDisplayModel> models = displayMapper.map(rooms);
        renderer.render(models);
    }

    // Cập nhật nhanh giá trên card lobby mà không cần render lại toàn bộ danh sách.
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
            LOGGER.error("Failed to handle UPDATE_PRICE.", e);
        }
    }
}
