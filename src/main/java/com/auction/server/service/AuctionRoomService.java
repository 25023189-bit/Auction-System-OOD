package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Bidder;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;

/**
 * Lớp AuctionRoomService xử lý tất cả các nghiệp vụ lõi liên quan đến Phòng Đấu Giá.
 * Giao tiếp 100% với SQL thông qua các DAO.
 */
public class AuctionRoomService {

    public Message joinRoom(String roomId) {
        // Lấy thông tin phòng từ Database
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room != null) {
            return new Message("ROOM_JOINED", "SERVER", room);
        }
        return new Message("ROOM_FAIL", "SERVER", "Không tìm thấy phòng!");
    }

    public Message placeNewBid(String roomId, String userId, double amount) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        if (user != null && user instanceof Bidder) {
            Bidder bidder = (Bidder) user;

            // 1. KIỂM TRA SỐ DƯ (Tránh việc Bid 1000$ nhưng ví chỉ có 100$)
            // Tuyệt đối không trừ tiền ở đây, chỉ kiểm tra!
            if (!bidder.canAfford(amount)) {
                return new Message("BID_FAIL", "SERVER", "Số dư ví không đủ " + amount + "$ để đặt giá!");
            }

            // 2. GỌI BidDAO ĐỂ THỰC HIỆN ĐẶT GIÁ (Dùng Transaction SQL an toàn)
            BidDAO bidDAO = new BidDAO();
            boolean isSuccess = bidDAO.placeBid(roomId, userId, amount);

            if (isSuccess) {
                // Trả về kèm tên thật của người dùng để Client hiển thị
                return new Message("BID_SUCCESS", bidder.getUsername(), amount);
            } else {
                return new Message("BID_FAIL", "SERVER", "Giá phải cao hơn mức hiện tại!");
            }
        }
        return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy người dùng hoặc bạn không phải người mua!");
    }

    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        System.out.println("🔍 [Hệ thống] Đang kiểm duyệt vật phẩm: " + itemName + " từ Seller: " + sellerName);
        if (startingPrice > 0 && itemName != null && !itemName.trim().isEmpty()) {
            return true;
        }
        return false;
    }
}