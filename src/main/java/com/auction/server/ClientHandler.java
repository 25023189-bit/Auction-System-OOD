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
            System.out.println("Error creating communication flow with Client!");
            e.printStackTrace();
        }
    }

    public String getCurrentRoomId() { return currentRoomId; }
    public boolean isAlive() { return alive && socket != null && !socket.isClosed(); }

    @Override
    public void run() {
        try {
            while (alive) {
                Message msg = (Message) in.readObject();
                if (msg == null) break;

                switch (msg.action) {
                    case "LOGIN":
                        // Client gửi data dạng: "password|role"
                        String[] loginParts = ((String) msg.data).split("\\|");
                        if (loginParts.length >= 2) {
                            String password = loginParts[0];
                            String role = loginParts[1];
                            Message loginRes = authService.login(msg.id, password, role);
                            if ("LOGIN_SUCCESS".equals(loginRes.action)) {
                                User loggedInUser = (User) loginRes.data;
                                this.userId = loggedInUser.getId();
                            }
                            sendMessage(loginRes);
                        }
                        break;

                    case "REGISTER":
                        String[] regData = ((String) msg.data).split("\\|");
                        if (regData.length >= 3) {
                            String uname = regData[0];
                            String passReg = regData[1];
                            String roleReg = regData[2];

                            User newUser = "SELLER".equalsIgnoreCase(roleReg)
                                    ? new Seller(msg.id, uname)
                                    : new Bidder(msg.id, uname);

                            Message response = authService.registerUser(newUser, passReg);
                            sendMessage(response);
                        }
                        break;

                    case "JOIN_ROOM":
                        this.currentRoomId = (msg.data != null) ? (String) msg.data : msg.id;
                        // Sửa lỗi: Truyền 2 tham số (roomId, userId)
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
                        handleBid(msg);
                        break;

                    case "CHAT_MSG":
                        handleChat(msg);
                        break;

                    case "RESET_PASSWORD":
                        Message resetResult = authService.resetPassword(msg.id, (String) msg.data);
                        sendMessage(resetResult);
                        break;

                    case "CREATE_AUCTION":
                        handleCreateAuction(msg);
                        break;

                    case "GET_ROOMS":
                        handleGetRooms();
                        break;

                    case "CLOSE_AUCTION":
                        handleCloseAuction(msg);
                        break;

                    case "ADMIN_GET_USERS":
                        List<User> userList = new UserDAO().getAllUsers();
                        sendMessage(new Message("ADMIN_USER_LIST", "SERVER", userList));
                        break;

                    case "ADMIN_GET_AUCTIONS":
                        List<AuctionRoom> adminRooms = new AuctionDAO().getAllAuctions();
                        sendMessage(new Message("ADMIN_AUCTION_LIST", "SERVER", adminRooms));
                        break;

                    case "GET_BID_HISTORY":
                        List<BidTransaction> history = new TransactionDAO().getHistoryByRoom((String) msg.data);
                        sendMessage(new Message("BID_HISTORY_SUCCESS", "SERVER", history));
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Client disconnected: " + userId);
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(Message response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (Exception e) {
            closeConnection();
        }
    }

    // Đổi tên thành closeConnection để khớp với AuctionServer
    public void closeConnection() {
        alive = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
        AuctionServer.removeClient(this);
    }

    private void handleBid(Message msg) {
        try {
            double bidAmount = (Double) msg.data;
            if (currentRoomId.isEmpty()) {
                sendMessage(new Message("BID_FAIL", "SERVER", "Chưa tham gia phòng!"));
                return;
            }
            Message res = roomService.placeNewBid(currentRoomId, userId, bidAmount);
            sendMessage(res);
            if ("BID_SUCCESS".equals(res.action)) {
                AuctionServer.broadcastAll(new Message("UPDATE_PRICE", "SERVER", currentRoomId + "|" + bidAmount + "|" + userId));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleChat(Message msg) {
        User sender = new UserDAO().getUserById(msg.id);
        String name = (sender != null) ? sender.getUsername() : "Khách";
        Message bMsg = new Message("CHAT_MSG", msg.id, name, msg.data);
        for (ClientHandler c : AuctionServer.clients) {
            if (c.isAlive() && currentRoomId.equals(c.getCurrentRoomId())) {
                c.sendMessage(bMsg);
            }
        }
    }

    private void handleGetRooms() {
        List<AuctionRoom> rooms = new AuctionDAO().getAllActiveAuctions();
        StringBuilder sb = new StringBuilder();
        for (AuctionRoom r : rooms) {
            sb.append(r.getRoomId()).append("|").append(r.getItemName()).append("|").append(r.getCurrentPrice()).append(";");
        }
        sendMessage(new Message("ROOM_LIST", "SERVER", sb.toString()));
    }

    private void handleCreateAuction(Message msg) {
        // ... (Giữ nguyên logic tạo phiên của bạn)
    }

    private void handleCloseAuction(Message msg) {
        // ... (Giữ nguyên logic đóng phiên của bạn)
    }
}