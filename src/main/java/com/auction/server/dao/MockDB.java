package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Lớp MockDB đóng vai trò như một cơ sở dữ liệu tạm thời trong bộ nhớ (RAM).
 * Giúp hệ thống chạy được ngay cả khi chưa kết nối MySQL hoặc dùng để test nhanh.
 */
public class MockDB {
    // 1. Bảng User: Mã User -> User Object
    public static Map<String, User> userTable = new HashMap<>();
    public static int userCounter = 3;

    // 2. Bảng Phòng: Mã Phòng -> AuctionRoom Object
    public static Map<String, AuctionRoom> auctionTable = new HashMap<>();

    // 3. Bảng Kho đồ (Inventory): Mã User (ID) -> Danh sách tên vật phẩm đã mua
    public static Map<String, java.util.ArrayList<String>> userInventory = new java.util.HashMap<>();

    static {
        userTable.put("BD000001", new Bidder("BD000001","Adam", "123456", 10000));
        userTable.put("BD000002", new Bidder("BD000002","Eva", "123456", 10000));
        userTable.put("BD000003", new Seller("BD000003","Zeus", "123456", 10000));


        AuctionRoom room1 = new AuctionRoom("AU00441", "Tên lửa Rocket.", 1000.0,"zeus");
        AuctionRoom room2 = new AuctionRoom("AU00442", "Siêu xe Bugatti.", 5000.0,"zeus");

        auctionTable.put(room1.getRoomId(), room1);
        auctionTable.put(room2.getRoomId(), room2);

        System.out.println("Input Available Data Successful.");
    }
}