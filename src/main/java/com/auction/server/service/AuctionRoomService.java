package com.auction.server.service;

// Import đúng package com.auction.common.dto
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.dao.MockDB;

/**
 * Lớp AuctionRoomService xử lý tất cả các nghiệp vụ lõi liên quan đến Phòng Đấu Giá.
 * Chịu trách nhiệm tương tác với Database (MockDB) và đảm bảo tính toàn vẹn dữ liệu.
 */
public class AuctionRoomService {

    /**
     * Xử lý nghiệp vụ tham gia vào một phòng đấu giá.
     */
    public com.auction.common.dto.Message joinRoom(String roomId) {
        // Lấy phòng từ MockDB
        AuctionRoom room = MockDB.auctionTable.get(roomId);

        if (room != null) {
            // Khớp với Constructor: Message(String action, String id, Object data)
            return new com.auction.common.dto.Message("ROOM_JOINED", "SERVER", (Object) room);
        }
        return new com.auction.common.dto.Message("ROOM_FAIL", "SERVER", (Object) "Không tìm thấy phòng!");
    }

    /**
     * Xử lý nghiệp vụ Đặt giá (Bid).
     */
    public com.auction.common.dto.Message processBid(String roomId, String username, double amount) {
        AuctionRoom currentRoom = MockDB.auctionTable.get(roomId);

        if (currentRoom == null) {
            return new com.auction.common.dto.Message("BID_FAIL", "SERVER", (Object) "Error: Auction not Exists.");
        }

        // Logic đặt giá mới
        boolean isSuccess = currentRoom.placeNewBid(username, amount);

        if (isSuccess) {
            // Khớp với Constructor: Message(String action, Object data, String role/username)
            return new com.auction.common.dto.Message("NEW_BID", (Object) amount, username);
        } else {
            return new com.auction.common.dto.Message("BID_FAIL", "SERVER", (Object) ("Lỗi: Giá phải cao hơn " + currentRoom.getCurrentPrice()));
        }
    }

    /**
     * Kiểm tra tính hợp lệ của vật phẩm trước khi tạo phòng.
     */
    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        System.out.println("🔍 [Hệ thống] Đang kiểm duyệt vật phẩm: " + itemName + " từ Seller: " + sellerName);

        // Duyệt tự động nếu giá > 0 và tên không rỗng
        if (startingPrice > 0 && itemName != null && !itemName.trim().isEmpty()) {
            return true;
        }
        return false;
    }
}