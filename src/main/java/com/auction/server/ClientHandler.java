package com.auction.server;

import com.auction.common.dto.Message;
import com.auction.common.model.*;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.main.AuctionServer;
import com.auction.server.service.AuthService;
import com.auction.server.service.AuctionRoomService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
    private String userId = "";
    public String getUserId() { return this.userId; }

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
                        Message loginRes = authService.login(msg.id, (String) msg.data);
                        // Nếu đăng nhập thành công, lưu ID lại để sau này Admin còn biết đường mà Kick!
                        if ("LOGIN_SUCCESS".equals(loginRes.action)) {
                            User loggedInUser = (User) loginRes.data;
                            this.userId = loggedInUser.getId();
                        }
                        sendMessage(loginRes);
                        break;

                    case "REGISTER":
                        String regUser = msg.id;

                        String[] regData = ((String) msg.data).split("\\|");

                        if (regData.length >= 3) {
                            String uname = regData[0];   // Username
                            String pass  = regData[1];   // Password
                            String role  = regData[2];   // Role

                            // Chuyển cho AuthService xử lý
                            Message response = authService.register(uname, pass, role);
                            sendMessage(response);
                        }
                        break;

                    case "JOIN_ROOM":
                        currentRoomId = (String) msg.data;
                        sendMessage(roomService.joinRoom(currentRoomId));
                        break;

                    case "LEAVE_ROOM":
                        currentRoomId = "";
                        break;

                    case "BID":
                        try {
                            double bidAmount = (Double) msg.data;
                            String userId = msg.id;

                            if (this.currentRoomId == null || this.currentRoomId.isEmpty()) {
                                sendMessage(new Message("BID_FAIL", "SERVER", "Chưa tham gia phòng nào!"));
                                break;
                            }

                            // 🌟 Chuyển lại việc xử lý nghiệp vụ cho Service
                            Message bidResult = roomService.placeNewBid(this.currentRoomId, userId, bidAmount);

                            if (bidResult.action.equals("BID_SUCCESS")) {
                                // Báo thành công cho người đặt
                                sendMessage(new Message("BID_SUCCESS", "SERVER", bidAmount));

                                // bidResult.id lúc này đang chứa TÊN NGƯỜI ĐẶT (do Service trả về)
                                String broadcastPayload = this.currentRoomId + "|" + bidAmount + "|" + bidResult.id;
                                AuctionServer.broadcastAll(new Message("UPDATE_PRICE", "SERVER", broadcastPayload));
                            } else {
                                // Gửi thông báo lỗi (Ví dụ: Không đủ tiền, giá thấp hơn...) về lại Client
                                sendMessage(bidResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case "CHAT_MSG":
                        String senderId = msg.id;

                        // 🌟 THAY MockDB BẰNG UserDAO
                        com.auction.server.dao.UserDAO chatUserDAO = new com.auction.server.dao.UserDAO();
                        User sender = chatUserDAO.getUserById(senderId);
                        String realUsername = (sender != null) ? sender.getUsername() : "Khách";

                        Message broadcastMsg = new Message("CHAT_MSG", senderId, realUsername, msg.data);
                        AuctionServer.broadcastAll(broadcastMsg);
                        break;

                    case "RESET_PASSWORD":
                        Message resetResult = authService.resetPassword(msg.id, (String) msg.data);

                        out.writeObject(resetResult);
                        out.flush();
                        break;
                    case "CREATE_AUCTION":
                        try {
                            String[] parts = ((String) msg.data).split("\\|");
                            if (parts.length < 3) throw new Exception("Dữ liệu không đủ 3 phần!");

                            String itemName = parts[0];
                            String itemDesc = parts[1];
                            double startingPrice = Double.parseDouble(parts[2]);
                            String sellerId = msg.id;

                            // --- 1. TẠO ID VÀ LƯU VÀO BẢNG ITEMS ---
                            // Vì SQL của bạn dùng VARCHAR(50) cho item_id, ta sinh ID dạng String
                            String newItemId = "IT" + (System.currentTimeMillis() % 1000000);
                            Item tempItem = new Item(newItemId, itemName, itemDesc, startingPrice);

                            // Dùng ItemDAO (vì bảng trong SQL là 'items')
                            com.auction.server.dao.ItemDAO itemDAO = new com.auction.server.dao.ItemDAO();
                            boolean isItemSaved = itemDAO.saveItem(tempItem);

                            if (!isItemSaved) {
                                throw new Exception("Không thể lưu Item vào Database!");
                            }

                            // --- 2. LẤY TÊN THẬT CỦA SELLER ---
                            com.auction.server.dao.UserDAO userDAO = new com.auction.server.dao.UserDAO();
                            User sellerObj = userDAO.getUserById(sellerId);
                            String trueSellerName = (sellerObj != null) ? sellerObj.getUsername() : sellerId;

                            // --- 3. LƯU VÀO BẢNG AUCTIONS ---
                            // Tạo Room ID (Ví dụ: AU100045)
                            String newRoomId = "AU1" + String.format("%05d", (System.currentTimeMillis() % 100000));
                            AuctionRoom newRoom = new AuctionRoom(newRoomId, itemName, startingPrice, trueSellerName);

                            com.auction.server.dao.AuctionDAO auctionDAO = new com.auction.server.dao.AuctionDAO();
                            // Truyền newItemId (String) vào thay vì int
                            boolean isAuctionSaved = auctionDAO.saveAuction(newRoom, newItemId, sellerId);

                            if (isAuctionSaved) {

                                System.out.println("✅ Đã tạo phiên đấu giá thành công: " + newRoomId);

                                // Phản hồi cho Seller
                                sendMessage(new Message("CREATE_AUCTION_SUCCESS", newRoomId, "Tạo thành công"));

                                // Broadcast danh sách phòng mới cho tất cả mọi người
                                java.util.List<AuctionRoom> activeRooms = auctionDAO.getAllActiveAuctions();
                                StringBuilder roomsInfo = new StringBuilder();
                                for (AuctionRoom room : activeRooms) {
                                    roomsInfo.append(room.getRoomId()).append("|")
                                            .append(room.getItemName()).append("|")
                                            .append(room.getCurrentPrice()).append(";");
                                }
                                AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));
                            } else {
                                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Lỗi lưu phiên đấu giá!"));
                            }

                        } catch (Exception e) {
                            System.err.println("❌ Lỗi CREATE_AUCTION: " + e.getMessage());
                            e.printStackTrace();
                            sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Lỗi: " + e.getMessage()));
                        }
                        break;

                    case "GET_ROOMS":
                        // 🌟 THAY MockDB BẰNG AuctionDAO
                        AuctionDAO getRoomDao = new AuctionDAO();
                        java.util.List<AuctionRoom> allRooms = getRoomDao.getAllActiveAuctions();

                        StringBuilder roomsInfoString = new StringBuilder();
                        for (AuctionRoom room : allRooms) {
                            roomsInfoString.append(room.getRoomId()).append("|")
                                    .append(room.getItemName()).append("|")
                                    .append(room.getCurrentPrice()).append(";");
                        }

                        sendMessage(new Message("ROOM_LIST", "SERVER", roomsInfoString.toString()));
                        break;

                    case "CLOSE_AUCTION":
                        handleCloseAuction(msg);
                        break;

                    // ==========================================================
                    // QUYỀN NĂNG TỐI CAO CỦA ADMIN
                    // ==========================================================

                    case "ADMIN_GET_USERS":
                        try {
                            UserDAO adminUserDao = new UserDAO();
                            List<User> userList = adminUserDao.getAllUsers();
                            System.out.println("SERVER TÌM THẤY: " + userList.size() + " USERS"); // Xem Server có móc được từ DB lên không
                            sendMessage(new Message("ADMIN_USER_LIST", "SERVER", userList));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi tải danh sách người dùng."));
                        }
                        break;

                    case "ADMIN_GET_AUCTIONS":
                        try {
                            AuctionDAO adminAuctionDao = new AuctionDAO();
                            List<AuctionRoom> adminRooms = adminAuctionDao.getAllAuctions();
                            sendMessage(new Message("ADMIN_AUCTION_LIST", "SERVER", adminRooms));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi tải danh sách phiên đấu giá."));
                        }
                        break;

                    case "GET_BID_HISTORY":
                        String roomId = (String) msg.data;

                        TransactionDAO transactionDAO = new TransactionDAO();
                        List<BidTransaction> historyList = transactionDAO.getHistoryByRoom(roomId);

                        // 3. Đóng gói danh sách và gửi trả lại cho Client
                        Message responseMsg = new Message("BID_HISTORY_SUCCESS", "SERVER", historyList);
                        sendMessage(responseMsg); // Gửi qua Socket về lại Client
                        break;

                    case "ADMIN_DELETE_AUCTION":
                        try {
                            String targetRoomId = (String) msg.data;
                            com.auction.server.dao.AuctionDAO delAuctionDao = new com.auction.server.dao.AuctionDAO();

                            // Gọi hàm forceDeleteAuction (Update status thành 'CANCELED')
                            if (delAuctionDao.forceDeleteAuction(targetRoomId)) {
                                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "AUCTION_DELETED", "Đã ép hủy phiên đấu giá: " + targetRoomId));

                                // Bật "loa phường" giải tán những người đang xem phòng này
                                AuctionServer.broadcastAll(new Message("AUCTION_CLOSED_NOTIFY", "SERVER", targetRoomId));

                                // Cập nhật lại sảnh chính cho "dân thường"
                                java.util.List<AuctionRoom> activeRooms = delAuctionDao.getAllActiveAuctions();
                                StringBuilder roomsInfoStr = new StringBuilder();
                                for (AuctionRoom r : activeRooms) {
                                    roomsInfoStr.append(r.getRoomId()).append("|")
                                            .append(r.getItemName()).append("|")
                                            .append(r.getCurrentPrice()).append(";");
                                }
                                AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", roomsInfoStr.toString()));

                            } else {
                                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi: Không thể hủy phiên đấu giá này!"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "ADMIN_DELETE_USER":
                        try {
                            String targetUserId = (String) msg.data;
                            com.auction.server.dao.UserDAO delUserDao = new com.auction.server.dao.UserDAO();

                            // 1. Xóa dưới Database
                            if (delUserDao.deleteUser(targetUserId)) {
                                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "USER_DELETED", "Đã bay màu tài khoản: " + targetUserId));

                                // 2. TÌM VÀ KICK NGƯỜI DÙNG ĐÓ NẾU ĐANG ONLINE
                                // ⚠️ Chú ý: Đảm bảo AuctionServer của bạn có biến danh sách public static List<ClientHandler> clients
                                if (AuctionServer.clients != null) {
                                    for (ClientHandler client : AuctionServer.clients) {
                                        if (targetUserId.equals(client.getUserId())) {
                                            client.sendMessage(new Message("BANNED", "SERVER", "Tài khoản của bạn đã bị xóa bởi Admin!"));
                                            break; // Kick xong thì thoát vòng lặp
                                        }
                                    }
                                }
                            } else {
                                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi: Không thể xóa tài khoản. Có thể do họ đang có phiên đấu giá."));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        String sellerId = msg.id;
        String roomId = (String) msg.data;

        AuctionDAO auctionDAO = new AuctionDAO();
        Object[] result = auctionDAO.closeAuctionAndTransferMoney(roomId, sellerId);

        boolean isSuccess = (Boolean) result[0];
        String winnerId = (String) result[1];
        double finalPrice = (Double) result[2];

        if (isSuccess && winnerId != null) {
            System.out.println("✅ CHỐT ĐƠN: Người thắng " + winnerId + " với giá " + finalPrice + "$");

            // Lấy lại số dư mới từ DB để báo cho các Client cập nhật UI
            UserDAO userDAO = new UserDAO();
            User winner = userDAO.getUserById(winnerId);
            User seller = userDAO.getUserById(sellerId);

            if (winner != null) AuctionServer.broadcastAll(new Message("UPDATE_BALANCE", winnerId, winner.getBalance()));
            if (seller != null) AuctionServer.broadcastAll(new Message("UPDATE_BALANCE", sellerId, seller.getBalance()));

        } else if (winnerId == null) {
            System.out.println("⚠️ Phòng đã đóng nhưng không có ai mua (Ế hàng).");
        } else {
            System.out.println("❌ BÙNG KÈO: Giao dịch thất bại do " + result[3]);
        }

        // Phát lệnh giải tán phòng cho tất cả những người đang xem
        AuctionServer.broadcastAll(new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId));

        // Cập nhật lại danh sách phòng (lấy từ SQL) ở Sảnh
        java.util.List<AuctionRoom> allRooms = auctionDAO.getAllActiveAuctions();
        StringBuilder roomsInfo = new StringBuilder();
        for (AuctionRoom r : allRooms) {
            roomsInfo.append(r.getRoomId()).append("|")
                    .append(r.getItemName()).append("|")
                    .append(r.getCurrentPrice()).append(";");
        }
        AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));
    }
}