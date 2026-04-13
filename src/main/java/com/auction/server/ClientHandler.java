package com.auction.server;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.Item;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.TransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.main.AuctionServer;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.AuthService;
import com.auction.server.service.AuctionStateManager;

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

    private final AuthService authService = new AuthService();
    private final AuctionRoomService roomService = new AuctionRoomService();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Error creating communication streams with client!");
            e.printStackTrace();
            alive = false;
        }
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAlive() {
        return alive && socket != null && !socket.isClosed();
    }

    @Override
    public void run() {
        try {
            while (alive) {
                Object raw = in.readObject();
                if (!(raw instanceof Message msg)) {
                    continue;
                }

                if (msg == null || msg.getAction() == null) {
                    continue;
                }

                switch (msg.getAction()) {
                    case "LOGIN":
                        handleLogin(msg);
                        break;

                    case "REGISTER":
                        handleRegister(msg);
                        break;

                    case "JOIN_ROOM":
                        handleJoinRoom(msg);
                        break;

                    case "LEAVE_ROOM":
                        handleLeaveRoom();
                        break;

                    case "BID":
                        handleBid(msg);
                        break;

                    case "CHAT_MSG":
                        handleChat(msg);
                        break;

                    case "RESET_PASSWORD":
                        handleResetPassword(msg);
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
                        handleAdminGetUsers();
                        break;

                    case "ADMIN_GET_AUCTIONS":
                        handleAdminGetAuctions();
                        break;

                    case "GET_BID_HISTORY":
                        handleGetBidHistory(msg);
                        break;

                    case "ADMIN_DELETE_AUCTION":
                        handleAdminDeleteAuction(msg);
                        break;

                    case "ADMIN_DELETE_USER":
                        handleAdminDeleteUser(msg);
                        break;

                    default:
                        sendMessage(new Message("UNKNOWN_ACTION", "SERVER", "Action không được hỗ trợ: " + msg.getAction()));
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Client mất kết nối: " + userId + " | " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // =========================================================
    // LOGIN / REGISTER / AUTH
    // =========================================================

    private void handleLogin(Message msg) {
        try {
            String username = msg.getId();
            String password = msg.getData() != null ? msg.getData().toString() : "";

            Message loginRes = authService.login(username, password);

            if ("LOGIN_SUCCESS".equals(loginRes.getAction()) && loginRes.getData() instanceof User loggedInUser) {
                this.userId = loggedInUser.getId();
            }

            sendMessage(loginRes);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("LOGIN_FAIL", "SERVER", "Lỗi xử lý đăng nhập!"));
        }
    }

    private void handleRegister(Message msg) {
        try {
            String[] regData = msg.getData() != null ? msg.getData().toString().split("\\|") : new String[0];

            if (regData.length < 3) {
                sendMessage(new Message("REGISTER_FAIL", "SERVER", "Dữ liệu đăng ký không hợp lệ!"));
                return;
            }

            String username = regData[0].trim();
            String rawPassword = regData[1].trim();
            String role = regData[2].trim().toUpperCase();

            UserDAO userDAO = new UserDAO();
            String generatedCustomerId = userDAO.generateNextCustomerId();

            User user = new User(
                    generatedCustomerId,
                    username,
                    role,
                    "",
                    "BIDDER".equals(role) ? 1_000_000.0 : 0.0
            );

            Message response = authService.registerUser(user, rawPassword);
            sendMessage(response);

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("REGISTER_FAIL", "SERVER", "Lỗi xử lý đăng ký!"));
        }
    }

    private void handleResetPassword(Message msg) {
        try {
            Message resetResult = authService.resetPassword(msg.getId(), (String) msg.getData());
            sendMessage(resetResult);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("RESET_FAIL", "SERVER", "Lỗi xử lý đổi mật khẩu!"));
        }
    }

    // =========================================================
    // ROOM
    // =========================================================

    private void handleJoinRoom(Message msg) {
        try {
            String roomId = msg.getData() != null ? msg.getData().toString().trim() : "";
            if (roomId.isEmpty()) {
                sendMessage(new Message("ROOM_FAIL", "SERVER", "Thiếu mã phòng đấu giá!"));
                return;
            }

            this.currentRoomId = roomId;

            System.out.println("User [" + this.userId + "] vừa join phòng: [" + this.currentRoomId + "]");

            Message joinResult = roomService.joinRoom(this.currentRoomId, this.userId);

            if ("ROOM_FAIL".equals(joinResult.getAction())) {
                this.currentRoomId = "";
            }

            sendMessage(joinResult);
        } catch (Exception e) {
            e.printStackTrace();
            this.currentRoomId = "";
            sendMessage(new Message("ROOM_FAIL", "SERVER", "Lỗi xử lý vào phòng!"));
        }
    }

    private void handleLeaveRoom() {
        this.currentRoomId = "";
    }

    private void handleGetRooms() {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> allRooms = auctionDAO.getAllActiveAuctions();
            sendMessage(new Message("ROOM_LIST", "SERVER", allRooms));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ROOM_LIST", "SERVER", java.util.Collections.emptyList()));
        }
    }

    // =========================================================
    // BID / CHAT / HISTORY
    // =========================================================

    private void handleBid(Message msg) {
        try {
            if (this.currentRoomId == null || this.currentRoomId.isBlank()) {
                sendMessage(new Message("BID_FAIL", "SERVER", "Chưa tham gia phòng nào!"));
                return;
            }

            double bidAmount = parseBidAmount(msg.getData());
            Message bidResult = roomService.placeNewBid(this.currentRoomId, this.userId, bidAmount);

            if ("BID_SUCCESS".equals(bidResult.getAction()) || "BID_SUCCESS_EXTENDED".equals(bidResult.getAction())) {
                AuctionServer.broadcastToRoom(this.currentRoomId, bidResult);

                String updatePayload = this.currentRoomId + "|" + bidAmount;
                AuctionServer.broadcastAll(new Message("UPDATE_PRICE", "SERVER", updatePayload));
            } else {
                sendMessage(bidResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("BID_FAIL", "SERVER", "Lỗi xử lý đặt giá!"));
        }
    }

    private void handleChat(Message msg) {
        try {
            UserDAO userDAO = new UserDAO();
            User sender = userDAO.getUserById(this.userId);
            String realUsername = (sender != null && sender.getUsername() != null)
                    ? sender.getUsername()
                    : "Khách";

            Message broadcastMsg = new Message("CHAT_MSG", this.userId, realUsername, msg.getData());
            AuctionServer.broadcastToRoom(this.currentRoomId, broadcastMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGetBidHistory(Message msg) {
        try {
            String roomId = msg.getData() != null ? msg.getData().toString() : "";
            TransactionDAO transactionDAO = new TransactionDAO();
            List<BidTransaction> historyList = transactionDAO.getHistoryByRoom(roomId);
            sendMessage(new Message("BID_HISTORY_SUCCESS", "SERVER", historyList));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("BID_HISTORY_FAIL", "SERVER", "Không tải được lịch sử đấu giá!"));
        }
    }

    // =========================================================
    // CREATE / CLOSE AUCTION
    // =========================================================

    private void handleCreateAuction(Message msg) {
        try {
            String[] parts = msg.getData() != null ? msg.getData().toString().split("\\|") : new String[0];

            if (parts.length < 6) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Dữ liệu tạo phiên không hợp lệ!"));
                return;
            }

            String itemName = parts[0].trim();
            String itemDesc = parts[1].trim();
            double startingPrice = Double.parseDouble(parts[2].trim());
            LocalDateTime startTime = LocalDateTime.parse(parts[3].trim());
            int durationMinutes = Integer.parseInt(parts[4].trim());
            int extensionSeconds = Integer.parseInt(parts[5].trim());

            if (!roomService.validateAuctionItem(msg.getId(), itemName, startingPrice)) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Thông tin vật phẩm không hợp lệ!"));
                return;
            }

            if (durationMinutes <= 0) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Thời lượng phải lớn hơn 0 phút!"));
                return;
            }

            if (extensionSeconds < 1 || extensionSeconds > 120) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Gia hạn phải từ 1 đến 120 giây!"));
                return;
            }

            if (startTime.isBefore(LocalDateTime.now())) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Thời gian bắt đầu phải ở hiện tại hoặc tương lai!"));
                return;
            }

            String sellerId = msg.getId() != null ? msg.getId().trim().toUpperCase() : "";
            String newItemId = generateItemId();
            String newRoomId = generateAuctionId();
            LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

            Item item = new Item(newItemId, itemName, itemDesc, startingPrice);

            ItemDAO itemDAO = new ItemDAO();
            boolean itemSaved = itemDAO.saveItem(item);
            if (!itemSaved) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Không thể lưu vật phẩm!"));
                return;
            }

            UserDAO userDAO = new UserDAO();
            User seller = userDAO.getUserById(sellerId);
            String sellerName = seller != null ? seller.getUsername() : sellerId;

            AuctionRoom room = new AuctionRoom(newRoomId, itemName, startingPrice, sellerName);
            room.setItemId(newItemId);
            room.setItemDescription(itemDesc);
            room.setStartTime(startTime);
            room.setDurationMinutes(durationMinutes);
            room.setActualEndTime(endTime);
            room.setExtensionSeconds(extensionSeconds);
            room.setStatus(startTime.isAfter(LocalDateTime.now()) ? "OPEN" : "RUNNING");

            AuctionDAO auctionDAO = new AuctionDAO();
            boolean auctionSaved = auctionDAO.saveAuction(room, newItemId, sellerId);

            if (!auctionSaved) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Không thể lưu phiên đấu giá!"));
                return;
            }

            sendMessage(new Message("CREATE_AUCTION_SUCCESS", newRoomId, room));
            broadcastRoomList();
        } catch (Exception e) {
            System.err.println("❌ Lỗi CREATE_AUCTION: " + e.getMessage());
            e.printStackTrace();
            sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Lỗi: " + e.getMessage()));
        }
    }

    private void handleCloseAuction(Message msg) {
        try {
            String roomId = msg.getData() != null ? msg.getData().toString() : "";
            if (roomId.isBlank()) {
                sendMessage(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Thiếu mã phiên đấu giá!"));
                return;
            }

            String sellerId = this.userId;
            AuctionDAO auctionDAO = new AuctionDAO();

            boolean closed = auctionDAO.closeAuctionBySeller(roomId, sellerId);
            if (!closed) {
                sendMessage(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Không thể đóng phiên đấu giá này!"));
                return;
            }

            AuctionStateManager.removeState(roomId);

            sendMessage(new Message("CLOSE_AUCTION_SUCCESS", "SERVER", roomId));
            AuctionServer.broadcastToRoom(roomId, new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId));
            broadcastRoomList();
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Không thể đóng phiên đấu giá!"));
        }
    }

    // =========================================================
    // ADMIN
    // =========================================================

    private void handleAdminGetUsers() {
        try {
            UserDAO userDAO = new UserDAO();
            List<User> userList = userDAO.getAllUsers();
            sendMessage(new Message("ADMIN_USER_LIST", "SERVER", userList));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi tải danh sách người dùng."));
        }
    }

    private void handleAdminGetAuctions() {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> rooms = auctionDAO.getAllAuctions();
            sendMessage(new Message("ADMIN_AUCTION_LIST", "SERVER", rooms));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi tải danh sách phiên đấu giá."));
        }
    }

    private void handleAdminDeleteAuction(Message msg) {
        try {
            String targetRoomId = msg.getData() != null ? msg.getData().toString() : "";
            AuctionDAO auctionDAO = new AuctionDAO();

            if (auctionDAO.forceDeleteAuction(targetRoomId)) {
                AuctionStateManager.removeState(targetRoomId);
                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "AUCTION_DELETED", "Đã ép hủy phiên đấu giá: " + targetRoomId));
                AuctionServer.broadcastAll(new Message("AUCTION_CLOSED_NOTIFY", "SERVER", targetRoomId));
                broadcastRoomList();
            } else {
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Không thể hủy phiên đấu giá này!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi khi hủy phiên đấu giá!"));
        }
    }

    private void handleAdminDeleteUser(Message msg) {
        try {
            String targetUserId = msg.getData() != null ? msg.getData().toString().trim().toUpperCase() : "";
            UserDAO userDAO = new UserDAO();

            if (userDAO.deleteUser(targetUserId)) {
                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "USER_DELETED", "Đã xóa tài khoản: " + targetUserId));

                if (AuctionServer.clients != null) {
                    for (ClientHandler client : AuctionServer.clients) {
                        if (client != null && targetUserId.equals(client.getUserId())) {
                            client.sendMessage(new Message("BANNED", "SERVER", "Tài khoản của bạn đã bị xóa bởi Admin!"));
                            client.closeConnection();
                            break;
                        }
                    }
                }
            } else {
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Không thể xóa tài khoản."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Lỗi khi xóa tài khoản!"));
        }
    }

    // =========================================================
    // UTIL
    // =========================================================

    public void sendMessage(Message response) {
        try {
            if (!isAlive()) {
                return;
            }

            out.writeObject(response);
            out.flush();
            out.reset();
        } catch (Exception e) {
            closeConnection();
        }
    }

    public void closeConnection() {
        boolean wasAlive = alive;
        alive = false;
        currentRoomId = "";

        try {
            if (in != null) in.close();
        } catch (Exception ignored) {
        }

        try {
            if (out != null) out.close();
        } catch (Exception ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {
        }

        if (wasAlive) {
            AuctionServer.clients.remove(this);
        }
    }

    private void broadcastRoomList() {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> rooms = auctionDAO.getAllActiveAuctions();
            AuctionServer.broadcastAll(new Message("ROOM_LIST", "SERVER", rooms));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcastBalancesAfterTimeoutStatic(AuctionDAO.CloseAuctionResult result) {
        if (result == null || !result.isSuccess()) return;
        if (!"SOLD".equalsIgnoreCase(result.getFinalStatus())) return;

        if (result.getWinnerId() != null && result.getWinnerBalance() != null) {
            AuctionServer.broadcastAll(
                    new Message("UPDATE_BALANCE", result.getWinnerId(), result.getWinnerBalance())
            );
        }

        if (result.getSellerId() != null && result.getSellerBalance() != null) {
            AuctionServer.broadcastAll(
                    new Message("UPDATE_BALANCE", result.getSellerId(), result.getSellerBalance())
            );
        }
    }

    private void finalizeExpiredAuctionIfNeeded(String roomId) {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            AuctionRoom room = auctionDAO.getAuctionById(roomId);
            if (room == null) return;
            if (!("OPEN".equalsIgnoreCase(room.getStatus()) || "RUNNING".equalsIgnoreCase(room.getStatus()))) return;
        } catch (Exception ignored) {
        }
    }

    private double parseBidAmount(Object data) {
        if (data instanceof Double d) return d;
        if (data instanceof Integer i) return i.doubleValue();
        if (data instanceof Long l) return l.doubleValue();
        return Double.parseDouble(data.toString().trim());
    }

    private String generateItemId() {
        return "IT" + String.format("%05d", System.currentTimeMillis() % 100000);
    }

    private String generateAuctionId() {
        return "AU" + String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}