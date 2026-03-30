package server.database;

import common.models.Auctions.AuctionRoom;
import common.models.Person.User;

import java.util.HashMap;
import java.util.Map;

public class MockDB {
    // 1. Bảng User: Mã User -> User Object
    public static Map<String, User> userTable = new HashMap<>();
    public static int userCounter = 2;

    // 2. Bảng Phòng: Mã Phòng -> AuctionRoom Object
    public static Map<String, AuctionRoom> auctionTable = new HashMap<>();

    static {
        userTable.put("BD000001", new User("BD000001","Adam", "123456"));
        userTable.put("BD000002", new User("BD000002","Eva", "123456"));

        AuctionRoom room1 = new AuctionRoom("AU00441", "Tên lửa Rocket.", 1000.0);
        AuctionRoom room2 = new AuctionRoom("AU00442", "Siêu xe Bugatti.", 5000.0);

        auctionTable.put(room1.getRoomId(), room1);
        auctionTable.put(room2.getRoomId(), room2);

        System.out.println("Input Available Data Successful.");
    }
}