package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Bidder;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;

public class AuctionRoomService {

    public Message joinRoom(String roomId) {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return new Message("ROOM_FAIL", "SERVER", "Không tìm thấy phòng hoặc phiên đã tự động kết thúc!");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if ("SOLD".equalsIgnoreCase(room.getStatus())
                || "UNSOLD".equalsIgnoreCase(room.getStatus())
                || "CANCELED_BY_ADMIN".equalsIgnoreCase(room.getStatus())) {
            return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá này không còn khả dụng!");
        }

        if (room.getActualEndTime() != null && now.isAfter(room.getActualEndTime())) {
            auctionDAO.closeAuctionByTime(roomId);
            return new Message("ROOM_FAIL", "SERVER", "Phiên đấu giá đã hết giờ và đã được chốt tự động!");
        }

        return new Message("ROOM_JOINED", "SERVER", room);
    }

    public Message placeNewBid(String roomId, String userId, double amount) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserById(userId);

        if (!(user instanceof Bidder)) {
            return new Message("BID_FAIL", "SERVER", "Lỗi: Không tìm thấy người dùng hoặc bạn không phải người mua!");
        }

        Bidder bidder = (Bidder) user;

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) {
            return new Message("BID_FAIL", "SERVER", "Không tìm thấy phòng đấu giá hoặc phiên đã kết thúc!");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if ("SCHEDULED".equalsIgnoreCase(room.getStatus())
                && room.getStartTime() != null
                && now.isBefore(room.getStartTime())) {
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá chưa bắt đầu!");
        }

        if (room.getActualEndTime() != null && now.isAfter(room.getActualEndTime())) {
            auctionDAO.closeAuctionByTime(roomId);
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá đã hết giờ và đã được chốt tự động!");
        }

        if (!"RUNNING".equalsIgnoreCase(room.getStatus()) && !"SCHEDULED".equalsIgnoreCase(room.getStatus())) {
            return new Message("BID_FAIL", "SERVER", "Phiên đấu giá không ở trạng thái hợp lệ để đặt giá!");
        }

        if (!bidder.canAfford(amount)) {
            return new Message("BID_FAIL", "SERVER", "Số dư ví không đủ " + amount + "$ để đặt giá!");
        }

        BidDAO bidDAO = new BidDAO();
        boolean isSuccess = bidDAO.placeBid(roomId, userId, amount);

        if (isSuccess) {
            return new Message("BID_SUCCESS", bidder.getUsername(), amount);
        } else {
            return new Message("BID_FAIL", "SERVER", "Giá phải cao hơn mức hiện tại!");
        }
    }

    public boolean validateAuctionItem(String sellerName, String itemName, double startingPrice) {
        System.out.println("🔍 [Hệ thống] Đang kiểm duyệt vật phẩm: " + itemName + " từ Seller: " + sellerName);
        return startingPrice > 0 && itemName != null && !itemName.trim().isEmpty();
    }
}