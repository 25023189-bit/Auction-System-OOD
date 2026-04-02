package server.services;

import common.DTO.Message;
import common.models.Person.Bidder;
import common.models.Person.User;
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
     * @param userId Người thực hiện đặt giá
     * @param amount Số tiền đặt
     * @return Message báo thành công (gửi cho toàn phòng) hoặc lỗi (gửi riêng cho người đặt)
     */
    public Message processBid(String roomId, String userId, double amount) {
        AuctionRoom currentRoom = MockDB.auctionTable.get(roomId);
        if (currentRoom == null) {
            return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy phòng đấu giá.");
        }

        // 1. Lấy User từ DB
        User user = MockDB.userTable.get(userId);
        if (user == null) {
            return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy người dùng!");
        }

        // 2. Kiểm tra nghiệp vụ Người mua (Bidder)
        if (user instanceof Bidder) {
            Bidder bidder = (Bidder) user;

            // 3. Kiểm tra ví tiền
            if (bidder.canAfford(amount)) {

                // 4. Nhờ Model AuctionRoom cập nhật giá
                boolean isSuccess = currentRoom.placeNewBid(bidder.getUsername(), amount);

                if (isSuccess) {
                    bidder.deduct(amount); // Đặt giá thành công mới trừ tiền
                    return new Message("NEW_BID", bidder.getUsername(), amount);
                } else {
                    return new Message("BID_FAIL", "SERVER", "Lỗi: Giá phải cao hơn " + currentRoom.getCurrentPrice());
                }
            } else {
                return new Message("BID_FAIL", "SERVER", "Số dư trong ví không đủ để đặt mức giá này!");
            }
        } else {
            return new Message("BID_FAIL", "SERVER", "Lỗi: Người bán (Seller) không được phép tham gia đặt giá!");
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

    public Message placeNewBid(String roomId, String userId, double bidAmount) {
        AuctionRoom room = MockDB.auctionTable.get(roomId);
        User user = MockDB.userTable.get(userId);

        if (room != null && user instanceof Bidder) {
            Bidder bidder = (Bidder) user;

            // 1. "Soi ví" xem có đủ tiền mặt không (KHÔNG TRỪ TIỀN Ở ĐÂY)
            if (!bidder.canAfford(bidAmount)) {
                return new Message("BID_FAIL", "SERVER", "Ví của bạn không đủ " + bidAmount + "$");
            }

            // 2. Đủ tiền thì bắt đầu cập nhật "Biến treo"
            synchronized (room) {
                if (room.placeNewBid(userId, bidAmount)) {
                    return new Message("BID_SUCCESS", "SERVER", bidAmount);
                } else {
                    return new Message("BID_FAIL", "SERVER", "Giá đặt phải cao hơn " + room.getCurrentPrice() + "$");
                }
            }
        }
        return new Message("BID_FAIL", "SERVER", "Lỗi dữ liệu phòng hoặc user!");
    }

    public boolean validateAuction(String itemName, double startingPrice, String sellerName) {
        System.out.println("Kiểm duyệt yêu cầu tạo phòng từ Seller: " + sellerName);

        // TODO: (Dành cho Admin sau này)
        // 1. Lưu yêu cầu vào danh sách chờ duyệt (Pending List).
        // 2. Trả về false tạm thời để Client biết là phải đợi duyệt.
        // 3. Admin có màn hình riêng, bấm nút duyệt thì mới sinh ra phòng (AuctionRoom).

        // TẠM THỜI: Hệ thống mock mặc định duyệt tự động (Auto-approve) triển luôn!
        if (startingPrice > 0 && itemName != null && !itemName.trim().isEmpty()) {
            return true;
        }
        return false;
    }
}