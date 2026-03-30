package server.services;

import common.DTO.Message;
import server.database.MockDB;
import common.models.Auctions.AuctionRoom;

/**
 * Lớp AuctionRoomService xử lý tất cả các nghiệp vụ lõi liên quan đến Phòng Đấu Giá.
 * Chịu trách nhiệm tương tác với Database (MockDB) và đảm bảo tính toàn vẹn dữ liệu (VD: Giá bid sau phải cao hơn giá trước).
 * Trả về các đối tượng Message đã được đóng gói sẵn để Handler chỉ việc gửi đi.
 */

public class AuctionRoomService {

    /**
     * Xử lý nghiệp vụ tham gia vào một phòng đấu giá.
     * @param roomId Mã phòng cần tham gia
     * @return Message chứa thông tin phòng nếu thành công, hoặc thông báo lỗi nếu thất bại
     */
    public Message joinRoom(String roomId) {
        AuctionRoom room = MockDB.auctionTable.get(roomId);
        if (room != null) {
            return new Message("ROOM_JOINED", "SERVER", room);
        }
        return new Message("ROOM_FAIL", "SERVER", "Không tìm thấy phòng!");
    }

    /**
     * Xử lý nghiệp vụ Đặt giá (Bid).
     * @param roomId Mã phòng đang diễn ra đấu giá
     * @param username Người thực hiện đặt giá
     * @param amount Số tiền đặt
     * @return Message báo thành công (gửi cho toàn phòng) hoặc lỗi (gửi riêng cho người đặt)
     */
    public Message processBid(String roomId, String username, double amount) {
        AuctionRoom currentRoom = MockDB.auctionTable.get(roomId);
        if (currentRoom == null) {
            return new Message("BID_FAIL", "SERVER", "Errol: Auction not Exits.");
        }

        // Gọi logic tính toán bên trong Object AuctionRoom (Tránh Anemic Domain Model)
        boolean isSuccess = currentRoom.placeNewBid(username, amount);

        if (isSuccess) {
            // Nếu bid hợp lệ, trả về gói tin NEW_BID để handler Broadcast cho toàn phòng
            return new Message("NEW_BID", username, amount);
        } else {
            // Nếu bid thấp hơn giá hiện tại, trả về lỗi cho riêng người đó
            return new Message("BID_FAIL", "SERVER", "Lỗi: Giá phải cao hơn " + currentRoom.getCurrentPrice());
        }
    }

    /**
     * HÀM KIỂM TRA TRUNG GIAN (MIDDLEWARE) TRƯỚC KHI TẠO PHÒNG
     * @param sellerName Tên người bán
     * @param itemName Tên vật phẩm
     * @param startingPrice Giá khởi điểm
     * @return true nếu hợp lệ và được phép tạo phòng, false nếu bị từ chối
     */
    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        /*
         * TODO CHỨC NĂNG MỞ RỘNG CHO TEAM:
         * 1. Check bộ lọc từ ngữ nhạy cảm (Profanity Filter) trong itemName.
         * 2. Tích hợp AI hoặc Admin duyệt tay trước khi trả về true.
         * 3. Kiểm tra hạng mức tài khoản của Seller (Ví dụ: Seller mới chỉ được bán đồ dưới 1000$).
         */

        System.out.println("🔍 [Hệ thống] Đang kiểm duyệt vật phẩm: " + itemName + " từ Seller: " + sellerName);

        // Hiện tại hệ thống mock mặc định duyệt tự động (Auto-approve) nếu giá > 0
        if (startingPrice > 0 && itemName != null && !itemName.trim().isEmpty()) {
            return true;
        }
        return false;
    }
}