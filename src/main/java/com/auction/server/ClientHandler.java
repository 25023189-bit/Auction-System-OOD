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
import java.time.LocalDateTime;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String currentRoomId = "";
    private String userId = "";
    private volatile boolean alive = true;
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
    public boolean isAlive() {
        return alive && socket != null && !socket.isClosed();
    }

    @Override
    public void run() {
        try {
            while (alive) {
                Message msg = (Message) in.readObject();
                if (msg == null) break;

                switch (msg.action) {
                    case "LOGIN":
                        String[] loginData = ((String) msg.data).split("\\|");
                        if (loginData.length >= 2) {
                            String password = loginData[0];
                            String role = loginData[1];
                            Message loginRes = authService.login(msg.id, password, role);
                            if ("LOGIN_SUCCESS".equals(loginRes.action)) {
                                User loggedInUser = (User) loginRes.data;
                                this.userId = loggedInUser.getId();
                            }
                            sendMessage(loginRes);
                        }
                        break;

                    case "REGISTER":
                        String regUser = msg.id;
                        String[] regData = ((String) msg.data).split("\\|");

                        if (regData.length >= 3) {
                            String uname = regData[0];   // Username
                            String passReg  = regData[1];   // Password
                            String roleReg  = regData[2];   // Role

                            // FIX LỖI CONSTRUCTOR: Dùng constructor 4 tham số (id, username, email trống, số dư ban đầu 0.0)
                            User newUser;
                            if ("SELLER".equalsIgnoreCase(roleReg)) {
                                newUser = new Seller(regUser, uname, "", 0.0);
                            } else {
                                newUser = new Bidder(regUser, uname, "", 0.0);
                            }

                            Message response = authService.registerUser(newUser, passReg);
                            sendMessage(response);
                        }
                        break;

                    case "JOIN_ROOM":
                        if (msg.data != null && !msg.data.toString().trim().isEmpty()) {
                            this.currentRoomId = (String) msg.data;
                        } else if (msg.id != null && !msg.id.trim().isEmpty()) {
                            this.currentRoomId = msg.id;
                        } else {
                            this.currentRoomId = "";
                        }

                        System.out.println("User [" + this.userId + "] vừa join phòng: [" + this.currentRoomId + "]");

                        Message joinResult = roomService.joinRoom(this.currentRoomId, this.userId);
                        if ("ROOM_FAIL".equals(joinResult.getAction())) {
                            this.currentRoomId = "";
                        }
                        sendMessage(joinResult);
                        break;

                    case "LEAVE_ROOM":
                        currentRoomId = "";
                        break;

                    case "BID":
                        try {
                            double bidAmount = (Double) msg.data;
                            String bidUserId = msg.id;

                            if (this.currentRoomId == null || this.currentRoomId.isEmpty()) {
                                sendMessage(new Message("BID_FAIL", "SERVER", "Chưa tham gia phòng nào!"));
                                break;
                            }

                            Message bidResult = roomService.placeNewBid(this.currentRoomId, bidUserId, bidAmount);

                            if (bidResult.action.equals("BID_SUCCESS")) {
                                sendMessage(new Message("BID_SUCCESS", "SERVER", bidAmount));

                                String broadcastPayload = this.currentRoomId + "|" + bidAmount + "|" + bidResult.id;
                                AuctionServer.broadcastAll(new Message("UPDATE_PRICE", "SERVER", broadcastPayload));
                            } else {
                                sendMessage(bidResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case "CHAT_MSG":
                        String senderId = msg.id;

                        com.auction.server.dao.UserDAO chatUserDAO = new com.auction.server.dao.UserDAO();
                        User sender = chatUserDAO.getUserById(senderId);
                        String realUsername = (sender != null) ? sender.getUsername() : "Khách";

                        Message broadcastMsg = new Message("CHAT_MSG", senderId, realUsername, msg.data);

                        if (AuctionServer.clients != null) {
                            for (ClientHandler client : AuctionServer.clients) {
                                if (client == null || !client.isAlive()) {
                                    AuctionServer.removeClient(client);
                                    continue;
                                }

                                if (this.currentRoomId != null && this.currentRoomId.equals(client.getCurrentRoomId())) {
                                    client.sendMessage(broadcastMsg);
                                }
                            }
                        }
                        break;

                    case "RESET_PASSWORD":
                        Message resetResult = authService.resetPassword(msg.id, (String) msg.data);
                        sendMessage(resetResult);
                        break;

                    case "CREATE_AUCTION":
                        try {
                            String[] parts = ((String) msg.data).split("\\|");

                            if (parts.length < 5) throw new Exception("Dữ liệu không đủ 5 phần (Bị thiếu thời gian)!");

                            String itemName = parts[0];
                            String itemDesc = parts[1];
                            double startingPrice = Double.parseDouble(parts[2]);
                            LocalDateTime startTime = LocalDateTime.parse(parts[3]);
                            int duration = Integer.parseInt(parts[4]);

                            if (duration <= 0) {
                                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Thời lượng phải lớn hơn 0 phút!"));
                                break;
                            }

                            if (startTime.isBefore(LocalDateTime.now())) {
                                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Thời gian bắt đầu phải ở hiện tại hoặc tương lai!"));
                                break;
                            }

                            LocalDateTime endTime = startTime.plusMinutes(duration);
                            String sellerId = msg.id;

                            String newItemId = "IT" + (System.currentTimeMillis() % 1000000);
                            Item tempItem = new Item(newItemId, itemName, itemDesc, startingPrice);

                            com.auction.server.dao.ItemDAO itemDAO = new com.auction.server.dao.ItemDAO();
                            boolean isItemSaved = itemDAO.saveItem(tempItem);

                            if (!isItemSaved) {
                                throw new Exception("Không thể lưu Item vào Database!");
                            }

                            com.auction.server.dao.UserDAO userDAO = new com.auction.server.dao.UserDAO();
                            User sellerObj = userDAO.getUserById(sellerId);
                            String trueSellerName = (sellerObj != null) ? sellerObj.getUsername() : sellerId;

                            String newRoomId = "AU1" + String.format("%05d", (System.currentTimeMillis() % 100000));
                            AuctionRoom newRoom = new AuctionRoom(newRoomId, itemName, startingPrice, trueSellerName);

                            newRoom.setStartTime(startTime);
                            newRoom.setDurationMinutes(duration);
                            newRoom.setActualEndTime(endTime);

                            com.auction.server.dao.AuctionDAO auctionDAO = new com.auction.server.dao.AuctionDAO();
                            boolean isAuctionSaved = auctionDAO.saveAuction(newRoom, newItemId, sellerId);

                            if (isAuctionSaved) {
                                sendMessage(new Message("CREATE_AUCTION_SUCCESS", newRoomId, "Tạo thành công"));

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
                        try {
                            System.out.println("User [" + this.userId + "] đang yêu cầu lấy danh sách phòng...");
                            com.auction.server.dao.AuctionDAO getRoomDao = new com.auction.server.dao.AuctionDAO();
                            java.util.List<AuctionRoom> allRooms = getRoomDao.getAllActiveAuctions();

                            StringBuilder roomsInfoString = new StringBuilder();

                            if (allRooms != null && !allRooms.isEmpty()) {
                                for (AuctionRoom room : allRooms) {
                                    String rId = room.getRoomId() != null ? room.getRoomId() : "Unknown";
                                    String rName = room.getItemName() != null ? room.getItemName() : "No Name";
                                    double rPrice = room.getCurrentPrice();

                                    roomsInfoString.append(rId).append("|")
                                            .append(rName).append("|")
                                            .append(rPrice).append(";");
                                }
                            }
                            sendMessage(new Message("ROOM_LIST", "SERVER", roomsInfoString.toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(new Message("ROOM_LIST", "SERVER", ""));
                        }
                        break;

                    case "CLOSE_AUCTION":
                        handleCloseAuction(msg);
                        break;

                    case "ADMIN_GET_USERS":
                        try {
                            UserDAO adminUserDao = new UserDAO();
                            List<User> userList = adminUserDao.getAllUsers();
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
                        sendMessage(new Message("BID_HISTORY_SUCCESS", "SERVER", historyList));
                        break;

                    case "ADMIN_DELETE_AUCTION":
                        try {
                            String targetRoomId = (String) msg.data;
                            com.auction.server.dao.AuctionDAO delAuctionDao = new com.auction.server.dao.AuctionDAO();

                            if (delAuctionDao.forceDeleteAuction(targetRoomId)) {
                                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "AUCTION_DELETED", "Đã ép hủy phiên đấu giá: " + targetRoomId));
                                AuctionServer.broadcastAll(new Message("AUCTION_CLOSED_NOTIFY", "SERVER", targetRoomId));

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

                            if (delUserDao.deleteUser(targetUserId)) {
                                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "USER_DELETED", "Đã bay màu tài khoản: " + targetUserId));

                                if (AuctionServer.clients != null) {
                                    for (ClientHandler client : AuctionServer.clients) {
                                        if (targetUserId.equals(client.getUserId())) {
                                            client.sendMessage(new Message("BANNED", "SERVER", "Tài khoản của bạn đã bị xóa bởi Admin!"));
                                            break;
                                        }
                                    }
                                }
                            } else {
                                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi: Không thể xóa tài khoản."));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Client mất kết nối: " + userId + " | " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(Message response) {
        try {
            if (!isAlive()) {
                closeConnection();
                return;
            }

            out.writeObject(response);
            out.flush();
            out.reset();
        } catch (Exception e) {
            closeConnection();
        }
    }

    // ĐÃ ĐỔI TÊN THÀNH closeConnection ĐỂ KHỚP VỚI AUCTION SERVER
    public void closeConnection() {
        alive = false;
        currentRoomId = "";

        try {
            if (in != null) in.close();
        } catch (Exception ignored) {}

        try {
            if (out != null) out.close();
        } catch (Exception ignored) {}

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}

        AuctionServer.removeClient(this);
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

        AuctionServer.broadcastAll(new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId));

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