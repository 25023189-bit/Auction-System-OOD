package com.auction.server;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;
import com.auction.server.dao.MockDB;
import com.auction.server.main.AuctionServer;
import com.auction.server.service.AuthService;
import com.auction.server.service.AuctionRoomService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Lớp ClientHandler chịu trách nhiệm duy trì kết nối 1-1 với một Client cụ thể.
 * Implement Runnable để mỗi Client kết nối đến sẽ được chạy trên một luồng (Thread) độc lập,
 * giúp Server có thể phục vụ hàng ngàn Client cùng lúc mà không bị nghẽn.
 * * Kiến trúc: Router/Controller - Nhận request từ Client -> Phân loại action -> Gọi Service xử lý -> Trả kết quả.
 */

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String currentRoomId = "";

    private AuthService authService = new AuthService();
    private AuctionRoomService roomService = new AuctionRoomService();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Error creating a communication flow with the Client!");
            e.printStackTrace();
        }
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                switch (msg.action) {
                    case "LOGIN":
                        sendMessage(authService.login(msg.id, (String) msg.data));
                        break;

                    case "REGISTER":
                        String regUser = msg.id;

                        String[] regData = ((String) msg.data).split("\\|");

                        String regPass = regData[0];
                        String regRole = regData[1];

                        Message regResponse = authService.register(regUser, regPass, regRole);

                        sendMessage(regResponse);
                        break;

                    case "JOIN_ROOM":
                        currentRoomId = (String) msg.data;
                        sendMessage(roomService.joinRoom(currentRoomId));
                        break;

                    case "LEAVE_ROOM":
                        currentRoomId = "";
                        break;

                    /*case "BID":
                        //handleBid(msg);
                        if (currentRoomId.isEmpty()) break;

                        Message bidResult = roomService.processBid(currentRoomId, msg.id, (double) msg.data);

                        if (bidResult.action.equals("NEW_BID")) {
                            AuctionServer.broadcastToRoom(currentRoomId, bidResult);
                        } else {
                            sendMessage(bidResult);
                        }
                        break;*/

                    case "BID":
                        try {
                            double bidAmount = (Double) msg.data;
                            String userId = msg.id;

                            if (this.currentRoomId == null || this.currentRoomId.isEmpty()) {
                                sendMessage(new Message("BID_FAIL", "SERVER", "Chưa tham gia phòng nào!"));
                                break;
                            }

                            Message result = roomService.placeNewBid(this.currentRoomId, userId, bidAmount);

                            // Đã sửa: Dùng hàm sendMessage để đồng bộ và tránh lỗi cache stream
                            sendMessage(result);

                            if (result.action.equals("BID_SUCCESS")) {
                                String broadcastPayload = this.currentRoomId + "|" + bidAmount + "|" + userId;
                                AuctionServer.broadcastAll(new Message("UPDATE_PRICE", "SERVER", broadcastPayload));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case "CHAT_MSG":
                        // 1. Nhận ID người gửi từ Client
                        String senderId = msg.id;

                        // 2. SERVER tự tìm tên người dùng trong MockDB của nó
                        User sender = MockDB.userTable.get(senderId);
                        String realUsername = (sender != null) ? sender.getUsername() : "Khách";

                        // 3. Đóng gói tin nhắn mới CÓ KÈM TÊN (Sử dụng constructor 4 tham số)
                        Message broadcastMsg = new Message("CHAT_MSG", senderId, realUsername, msg.data);

                        // 4. Phát đi cho các Client khác (Dùng hàm broadcast của bạn)
                        AuctionServer.broadcastAll(broadcastMsg);
                        // (Hoặc gửi cho những người trong cùng phòng tùy logic của bạn)
                        break;

                    case "RESET_PASSWORD":
                        Message resetResult = authService.resetPassword(msg.id, (String) msg.data);

                        out.writeObject(resetResult);
                        out.flush();
                        break;
                    case "CREATE_AUCTION":
                        String sellerName = msg.id; // Tên người bán
                        String[] auctionData = ((String) msg.data).split("\\|");

                        String itemName = auctionData[0];
                        double startingPrice = Double.parseDouble(auctionData[1]);

                        AuctionRoomService roomService = new AuctionRoomService();
                        boolean isApproved = roomService.validateAuctionItem(msg.id, itemName, startingPrice);
                        if (isApproved) {
                            String newRoomId = "AU" + System.currentTimeMillis();

                            User sellerObj = MockDB.userTable.get(msg.id);
                            String trueSellerName = (sellerObj != null) ? sellerObj.getUsername() : msg.id;

                            // Gắn tên thật vào phòng
                            AuctionRoom newRoom = new AuctionRoom(newRoomId, itemName, startingPrice, trueSellerName);

                            MockDB.auctionTable.put(newRoomId, newRoom);

                            sendMessage(new Message("CREATE_AUCTION_SUCCESS", newRoomId, "Tạo thành công"));

                            StringBuilder roomsInfo = new StringBuilder();
                            for (AuctionRoom room : MockDB.auctionTable.values()) {
                                roomsInfo.append(room.getRoomId()).append("|")
                                        .append(room.getItemName()).append("|")
                                        .append(room.getCurrentPrice()).append(";");
                            }

                            AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));

                        } else {
                            sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Sản phẩm bị từ chối kiểm duyệt!"));
                        }
                        break;
                    case "GET_ROOMS":
                        // Lục lọi trong kho MockDB xem có bao nhiêu phòng thì gom hết lại
                        StringBuilder roomsInfo = new StringBuilder();

                        for (AuctionRoom room : MockDB.auctionTable.values()) {
                            // Đóng gói theo chuẩn: Mã phòng|Tên sản phẩm|Giá hiện tại;
                            roomsInfo.append(room.getRoomId()).append("|")
                                    .append(room.getItemName()).append("|")
                                    .append(room.getCurrentPrice()).append(";");
                        }

                        // Gửi nguyên 1 cục chuỗi này về cho Client
                        sendMessage(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));
                        break;
                    case "CLOSE_AUCTION":
                        handleCloseAuction(msg);
                        break;
                }
            }
        } catch (Exception e) {
            // Xử lý ngắt kết nối
        }
    }

    /**
     * Gửi một thông điệp từ Server về cho Client này.
     *
     * @param response Đối tượng Message chứa kết quả xử lý
     */
    public void sendMessage(Message response) {
        try {
            out.writeObject(response);
            out.flush();
            out.reset();
        } catch (Exception e) {
            System.out.println("Error sending response to Client!");
        }
    }

    private void handleBid(Message msg) {
        try {
            // Cắt chuỗi payload "R01|550.0"
            String[] parts = ((String) msg.data).split("\\|");
            String roomId = parts[0];
            double bidAmount = Double.parseDouble(parts[1]);

            // Gọi Service xử lý lõi
            Message result = roomService.placeNewBid(roomId, msg.username, bidAmount);

            // Gửi kết quả (Thành công/Thất bại) cho riêng người vừa bấm nút
            sendMessage(result);

            // NẾU THÀNH CÔNG -> Bật "loa phường" thông báo cho TẤT CẢ mọi người cập nhật UI
            if (result.action.equals("BID_SUCCESS")) {
                // Đóng gói dữ liệu: Mã phòng | Giá mới | Tên người vừa đặt
                String broadcastPayload = roomId + "|" + bidAmount + "|" + msg.username;
                AuctionServer.broadcastAll(new Message("UPDATE_PRICE", "SERVER", broadcastPayload));
            }
        } catch (Exception e) {
            sendMessage(new Message("BID_FAIL", "SERVER", "Lệnh không hợp lệ!"));
        }
    }

    private void handleCloseAuction(Message msg) {
        String userId = msg.id; // ID của người bấm nút chốt (Seller)
        String roomId = (String) msg.data; // Mã phòng

        // Lấy thông tin Seller từ DB
        User sender = MockDB.userTable.get(userId);
        if (sender == null || !(sender instanceof Seller)) {
            return; // Báo lỗi nếu không phải Seller
        }
        Seller seller = (Seller) sender;

        // Lấy thông tin Phòng từ DB
        AuctionRoom room = MockDB.auctionTable.get(roomId);

        if (room != null) {
            synchronized (room) {
                // Kiểm tra xem ông này có đúng là chủ phòng không
                if (!room.getNameSeller().equalsIgnoreCase(seller.getUsername())) {
                    System.out.println("Lỗi: Không phải chủ phòng!");
                    return;
                }

                // =========================================================
                // LOGIC "TÍNH TIỀN" ĐỈNH CAO CỦA BẠN BẮT ĐẦU Ở ĐÂY
                // =========================================================
                String winnerId = room.getHighestBidderId(); // Lấy ID của người trả cao nhất
                double finalPrice = room.getCurrentPrice();  // Giá chốt cuối cùng

                // Nếu phòng có người mua (Không bị ế)
                if (!winnerId.equals("Chưa có ai")) {
                    User winnerUser = MockDB.userTable.get(winnerId);

                    if (winnerUser != null && winnerUser instanceof Bidder) {
                        Bidder winner = (Bidder) winnerUser;

                        // Tiến hành trừ tiền thật sự
                        if (winner.deduct(finalPrice)) {
                            // Cộng tiền cho chủ phòng
                            seller.addBalance(finalPrice);

                            // Thêm vật phẩm vào kho của người thắng
                            MockDB.userInventory.putIfAbsent(winnerId, new java.util.ArrayList<>());
                            MockDB.userInventory.get(winnerId).add(room.getItemName());

                            System.out.println("✅ CHỐT ĐƠN: " + winner.getUsername() + " mua " + room.getItemName() + " giá " + finalPrice + "$");
                            System.out.println("Tiền trong ví Bidder còn"+winner.getBalance());
                            System.out.println("Tiền trong ví Seller còn"+seller.getBalance());

                            // Phát loa cho toàn Server, ai đúng ID thì người đó sẽ tự nhận
                            AuctionServer.broadcastAll(new Message("UPDATE_BALANCE", winnerId, winner.getBalance()));
                            AuctionServer.broadcastAll(new Message("UPDATE_BALANCE", seller.getId(), seller.getBalance()));
                        } else {
                            System.out.println("❌ BÙNG KÈO: " + winner.getUsername() + " không đủ tiền thanh toán!");
                        }
                    }
                } else {
                    System.out.println("⚠️ Phòng đóng nhưng không có ai mua (Ế hàng).");
                }
                // =========================================================

                // Xóa phòng khỏi Database
                MockDB.auctionTable.remove(roomId);

                // Phát Broadcast cho toàn Server để các Client (Bidders) văng ra khỏi phòng
                AuctionServer.broadcastAll(new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId));

                // Cập nhật lại danh sách phòng ở Sảnh
                StringBuilder roomsInfo = new StringBuilder();
                for (AuctionRoom r : MockDB.auctionTable.values()) {
                    roomsInfo.append(r.getRoomId()).append("|")
                            .append(r.getItemName()).append("|")
                            .append(r.getCurrentPrice()).append(";");
                }
                AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));
            }
        }
    }
}