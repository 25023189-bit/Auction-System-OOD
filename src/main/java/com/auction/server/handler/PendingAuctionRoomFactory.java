package com.auction.server.handler;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;

import java.time.LocalDateTime;

/**
 * Factory chuyển yêu cầu tạo phiên đang chờ duyệt thành model lưu database.
 *
 * Vai trò:
 * - Tạo AuctionRoom từ PendingAuctionRequest sau khi admin approve.
 * - Tạo Item tương ứng để AuctionDAO lưu cùng phiên đấu giá.
 *
 * Luồng chính:
 * 1. AdminActionHandler lấy request đã approve và gọi factory.
 * 2. Factory copy dữ liệu seller/item/schedule sang AuctionRoom và Item.
 *
 * Business rules:
 * - Status của room là OPEN nếu startTime ở tương lai, RUNNING nếu đã đến thời điểm bắt đầu.
 * - Giá hiện tại ban đầu bằng startingPrice, duration/extension giữ nguyên theo request đã validate.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ tạo object mới từ request đầu vào.
 * - Dependency: PendingAuctionRequest, AuctionRoom, Item, LocalDateTime.
 */
public class PendingAuctionRoomFactory {
    public AuctionRoom createRoom(PendingAuctionRequest request) {
        // Room nhận lại toàn bộ thông tin seller đã submit, kèm trạng thái theo thời điểm bắt đầu.
        AuctionRoom room = new AuctionRoom(
                request.getRoomId(),
                request.getItemName(),
                request.getStartingPrice(),
                request.getSellerId()
        );
        room.setItemId(request.getItemId());
        room.setItemDescription(request.getItemDesc());
        room.setCurrentPrice(request.getStartingPrice());
        room.setStartingPrice(request.getStartingPrice());
        room.setMinimumJoinAmount(request.getMinimumJoinAmount());
        room.setBidStep(request.getBidStep());
        room.setStartTime(request.getStartTime());
        room.setDurationMinutes(request.getDurationMinutes());
        room.setActualEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
        room.setExtensionSeconds(request.getExtensionSeconds());
        room.setSellerReputation(request.getSellerReputation());
        room.setSellerSuccessfulAuctionRate(request.getSuccessfulAuctionRate());
        room.setSellerAdminCancellationRate(request.getAdminCancellationRate());
        room.setStatus(request.getStartTime().isAfter(LocalDateTime.now()) ? "OPEN" : "RUNNING");
        return room;
    }

    public Item createItem(PendingAuctionRequest request) {
        // Item là phần sản phẩm được tách riêng khỏi auction trong database.
        return new Item(
                request.getItemId(),
                request.getItemName(),
                request.getItemDesc(),
                request.getStartingPrice()
        );
    }
}
