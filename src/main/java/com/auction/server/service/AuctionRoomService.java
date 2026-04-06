package com.auction.server.service;

// Import đúng package com.auction.common.dto
import com.auction.common.dto.Message;
import com.auction.common.model.Bidder;
import com.auction.common.model.User;
import com.auction.server.dao.MockDB;
import com.auction.common.model.AuctionRoom;
import com.auction.server.dao.MockDB;

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
     * @param userId Người thực hiện đặt giá
     * @param amount Số tiền đặt
     * @return Message báo thành công (gửi cho toàn phòng) hoặc lỗi (gửi riêng cho người đặt)
     */
    public Message placeNewBid(String roomId, String userId, double amount) {
        com.auction.common.model.AuctionRoom currentRoom = MockDB.auctionTable.get(roomId);
        com.auction.common.model.User user = MockDB.userTable.get(userId);

        if (currentRoom != null && user instanceof com.auction.common.model.Bidder) {
            com.auction.common.model.Bidder bidder = (com.auction.common.model.Bidder) user;

            // 1. Chỉ kiểm tra ví, TUYỆT ĐỐI KHÔNG TRỪ TIỀN Ở ĐÂY
            if (!bidder.canAfford(amount)) {
                return new Message("BID_FAIL", "SERVER", "Số dư ví không đủ " + amount + "$ để đặt giá!");
            }

            // 2. Khóa phòng để cập nhật giá
            synchronized (currentRoom) {
                // Truyền userId vào biến treo để lúc chốt đơn Server biết ai để trừ tiền
                boolean isSuccess = currentRoom.placeNewBid(userId, amount);

                if (isSuccess) {
                    // Thành công -> Báo cho mọi người giá mới
                    return new Message("BID_SUCCESS", bidder.getUsername(), amount);
                } else {
                    return new Message("BID_FAIL", "SERVER", "Giá phải cao hơn mức hiện tại!");
                }
            }
        }
        return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy phòng hoặc bạn không phải người mua!");
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