package com.auction.server.dao;

// SỬA: Import đúng địa chỉ package thực tế của dự án
import com.auction.common.model.AuctionRoom;
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
    public static int userCounter = 2;

    // 2. Bảng Phòng: Mã Phòng -> AuctionRoom Object
    public static Map<String, AuctionRoom> auctionTable = new HashMap<>();

    // Khối static này sẽ chạy ngay khi ứng dụng khởi động để nạp dữ liệu mẫu
    static {
        try {
            // SỬA: Khởi tạo User mẫu (Đảm bảo Class User có Constructor: id, username, password)
            userTable.put("BD000001", new User("BD000001", "Adam", "123456"));
            userTable.put("BD000002", new User("BD000002", "Eva", "123456"));

            // SỬA: Khởi tạo Phòng đấu giá mẫu (Đảm bảo AuctionRoom có Constructor: id, itemName, startingPrice)
            AuctionRoom room1 = new AuctionRoom("AU00441", "Tên lửa Rocket.", 1000.0);
            AuctionRoom room2 = new AuctionRoom("AU00442", "Siêu xe Bugatti.", 5000.0);

            auctionTable.put(room1.getRoomId(), room1);
            auctionTable.put(room2.getRoomId(), room2);

            System.out.println("✅ [MockDB] Đã nạp dữ liệu mẫu thành công.");
        } catch (Exception e) {
            System.err.println("❌ [MockDB] Lỗi khi nạp dữ liệu mẫu: " + e.getMessage());
        }
    }
}