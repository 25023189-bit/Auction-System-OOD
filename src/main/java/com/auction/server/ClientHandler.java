package com.auction.server;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.ProductDetailResponse;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.main.AuctionServer;
import com.auction.server.service.AuctionCreationValidator;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.AuctionStateManager;
import com.auction.server.service.AuthService;
import com.auction.server.service.PendingAuctionApprovalService;
import com.auction.server.service.ProductDetailService;

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
    private final PendingAuctionApprovalService pendingAuctionApprovalService = new PendingAuctionApprovalService();

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

    public void leaveCurrentRoomIfMatches(String roomId) {
        if (roomId != null && roomId.equals(this.currentRoomId)) {
            this.currentRoomId = "";
        }
    }

    @Override
    public void run() {
        try {
            while (alive) {
                Object raw = in.readObject();
                if (!(raw instanceof Message msg)) {
                    continue;
                }

                if (msg.getAction() == null) {
                    continue;
                }

                switch (msg.getAction()) {
                    case "LOGIN" -> handleLogin(msg);
                    case "REGISTER" -> handleRegister(msg);
                    case "JOIN_ROOM" -> handleJoinRoom(msg);
                    case "LEAVE_ROOM" -> handleLeaveRoom();
                    case "BID" -> handleBid(msg);
                    case "CHAT_MSG" -> handleChat(msg);
                    case "RESET_PASSWORD" -> handleResetPassword(msg);
                    case "CREATE_AUCTION" -> handleCreateAuction(msg);
                    case "GET_ROOMS" -> handleGetRooms();
                    case "CLOSE_AUCTION" -> handleCloseAuction(msg);
                    case "ADMIN_GET_USERS" -> handleAdminGetUsers();
                    case "ADMIN_GET_AUCTIONS" -> handleAdminGetAuctions();
                    case "ADMIN_GET_PENDING_AUCTIONS" -> handleAdminGetPendingAuctions();
                    case "ADMIN_APPROVE_AUCTION" -> handleAdminApproveAuction(msg);
                    case "ADMIN_REJECT_AUCTION" -> handleAdminRejectAuction(msg);
                    case "GET_BID_HISTORY" -> handleGetBidHistory(msg);
                    case "ADMIN_DELETE_AUCTION" -> handleAdminDeleteAuction(msg);
                    case "ADMIN_DELETE_USER" -> handleAdminDeleteUser(msg);
                    case "GET_PRODUCT_DETAILS" -> handleGetProductDetails(msg);
                    default -> sendMessage(new Message("UNKNOWN_ACTION", "SERVER", "Unsupported action: " + msg.getAction()));
                }
            }
        } catch (Exception e) {
            System.out.println("Client mat ket noi: " + userId + " | " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

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
            sendMessage(new Message("LOGIN_FAIL", "SERVER", "Login processing error!"));
        }
    }

    private void handleRegister(Message msg) {
        try {
            String[] regData = msg.getData() != null ? msg.getData().toString().split("\\|", -1) : new String[0];
            if (regData.length < 4) {
                sendMessage(new Message("REGISTER_FAIL", "SERVER", "Invalid registration data!"));
                return;
            }

            String username = regData[0].trim();
            String rawPassword = regData[1].trim();
            String role = regData[2].trim().toUpperCase();
            String organization = regData[3].trim();

            UserDAO userDAO = new UserDAO();
            String generatedCustomerId = userDAO.generateNextCustomerId();

            User user = new User(
                    generatedCustomerId,
                    username,
                    role,
                    "",
                    "SELLER".equals(role) ? organization : null,
                    "BIDDER".equals(role) ? 1_000_000.0 : 0.0
            );

            Message response = authService.registerUser(user, rawPassword);
            sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("REGISTER_FAIL", "SERVER", "Registration processing error!"));
        }
    }

    private void handleResetPassword(Message msg) {
        try {
            Message resetResult = authService.resetPassword(msg.getId(), (String) msg.getData());
            sendMessage(resetResult);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("RESET_FAIL", "SERVER", "Password reset processing error!"));
        }
    }

    private void handleJoinRoom(Message msg) {
        try {
            String roomId = msg.getData() != null ? msg.getData().toString().trim() : "";
            if (roomId.isEmpty()) {
                sendMessage(new Message("ROOM_FAIL", "SERVER", "Missing auction room ID!"));
                return;
            }

            this.currentRoomId = roomId;
            Message joinResult = roomService.joinRoom(this.currentRoomId, this.userId);

            if ("ROOM_FAIL".equals(joinResult.getAction())) {
                this.currentRoomId = "";
            }

            sendMessage(joinResult);
            if ("ROOM_JOINED".equals(joinResult.getAction())) {
                AuctionServer.broadcastToRoom(this.currentRoomId, new Message("ROOM_STATE_UPDATED", "SERVER", joinResult.getData()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.currentRoomId = "";
            sendMessage(new Message("ROOM_FAIL", "SERVER", "Join room processing error!"));
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

    private void handleBid(Message msg) {
        try {
            if (this.currentRoomId.isBlank()) {
                sendMessage(new Message("BID_FAIL", "SERVER", "You have not joined any room!"));
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
            sendMessage(new Message("BID_FAIL", "SERVER", "Bid processing error!"));
        }
    }

    private void handleChat(Message msg) {
        try {
            UserDAO userDAO = new UserDAO();
            User sender = userDAO.getUserById(this.userId);
            String realUsername = (sender != null && sender.getUsername() != null) ? sender.getUsername() : "Khach";

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
            sendMessage(new Message("BID_HISTORY_FAIL", "SERVER", "Unable to load bid history!"));
        }
    }

    private void handleCreateAuction(Message msg) {
        try {
            String[] parts = msg.getData() != null ? msg.getData().toString().split("\\|", -1) : new String[0];
            if (parts.length < 8) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Invalid auction creation data!"));
                return;
            }

            String itemName = parts[0].trim();
            String itemDesc = parts[1].trim();
            double startingPrice = Double.parseDouble(parts[2].trim());
            double minimumJoinAmount = Double.parseDouble(parts[3].trim());
            double bidStep = Double.parseDouble(parts[4].trim());
            LocalDateTime startTime = LocalDateTime.parse(parts[5].trim());
            int durationMinutes = Integer.parseInt(parts[6].trim());
            int extensionSeconds = Integer.parseInt(parts[7].trim());

            String sellerId = msg.getId() != null ? msg.getId().trim().toUpperCase() : "";
            UserDAO userDAO = new UserDAO();
            User seller = userDAO.getUserById(sellerId);
            if (seller == null) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Seller account not found!"));
                return;
            }
            if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Only sellers can create auctions!"));
                return;
            }

            AuctionDAO auctionDAO = new AuctionDAO();
            AuctionDAO.SellerAuctionStats sellerStats = auctionDAO.getSellerAuctionStats(sellerId);
            seller.setSuccessfulAuctionRate(sellerStats.getSuccessfulAuctionRate());
            seller.setAdminCancellationRate(sellerStats.getAdminCancellationRate());

            AuctionCreationValidator validator = new AuctionCreationValidator();
            if (!validator.validateAuction(
                    sellerId,
                    seller.getOrganization(),
                    itemName,
                    itemDesc,
                    startingPrice,
                    minimumJoinAmount,
                    bidStep,
                    startTime,
                    durationMinutes,
                    extensionSeconds,
                    seller.getSellerReputation(),
                    seller.getSuccessfulAuctionRate(),
                    seller.getAdminCancellationRate()
            )) {
                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", validator.getErrorMessage()));
                return;
            }

            String newItemId = generateItemId();
            String newRoomId = generateAuctionId();

            PendingAuctionRequest request = new PendingAuctionRequest(
                    generatePendingRequestId(),
                    newRoomId,
                    newItemId,
                    sellerId,
                    seller.getOrganization(),
                    itemName,
                    itemDesc,
                    startingPrice,
                    minimumJoinAmount,
                    bidStep,
                    startTime,
                    durationMinutes,
                    extensionSeconds,
                    seller.getSellerReputation(),
                    seller.getSuccessfulAuctionRate(),
                    seller.getAdminCancellationRate()
            );
            pendingAuctionApprovalService.submit(request);

            sendMessage(new Message("CREATE_AUCTION_PENDING", request.getRequestId(),
                    "Auction request submitted and is waiting for admin approval."));
            broadcastPendingAuctionList();
        } catch (Exception e) {
            System.err.println("CREATE_AUCTION error: " + e.getMessage());
            e.printStackTrace();
            sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Error: " + e.getMessage()));
        }
    }

    private void handleCloseAuction(Message msg) {
        try {
            String roomId = msg.getData() != null ? msg.getData().toString() : "";
            if (roomId.isBlank()) {
                sendMessage(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Missing auction ID!"));
                return;
            }

            String sellerId = this.userId;
            AuctionDAO auctionDAO = new AuctionDAO();

            boolean closed = auctionDAO.closeAuctionBySeller(roomId, sellerId);
            if (!closed) {
                sendMessage(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Unable to close this auction!"));
                return;
            }

            AuctionStateManager.removeState(roomId);

            sendMessage(new Message("CLOSE_AUCTION_SUCCESS", "SERVER", roomId));
            AuctionServer.notifyRoomClosed(roomId);
            broadcastRoomList();
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("CLOSE_AUCTION_FAIL", "SERVER", "Unable to close auction!"));
        }
    }

    private void handleAdminGetUsers() {
        try {
            UserDAO userDAO = new UserDAO();
            List<User> userList = userDAO.getAllUsers();
            sendMessage(new Message("ADMIN_USER_LIST", "SERVER", userList));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to load user list."));
        }
    }

    private void handleAdminGetAuctions() {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> rooms = auctionDAO.getAllAuctions();
            sendMessage(new Message("ADMIN_AUCTION_LIST", "SERVER", rooms));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to load auction list."));
        }
    }

    private void handleAdminGetPendingAuctions() {
        try {
            sendMessage(new Message("ADMIN_PENDING_AUCTION_LIST", "SERVER", pendingAuctionApprovalService.getAllPending()));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to load pending auction requests."));
        }
    }

    private void handleAdminApproveAuction(Message msg) {
        try {
            String requestId = msg.getData() != null ? msg.getData().toString().trim() : "";
            PendingAuctionRequest request = pendingAuctionApprovalService.approve(requestId);
            if (request == null) {
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Pending auction request not found."));
                return;
            }

            AuctionRoom room = buildRoomFromPendingRequest(request);
            Item item = new Item(request.getItemId(), request.getItemName(), request.getItemDesc(), request.getStartingPrice());
            AuctionDAO auctionDAO = new AuctionDAO();

            if (!auctionDAO.createAuctionWithItem(room, item, request.getSellerId())) {
                pendingAuctionApprovalService.submit(request);
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to approve auction request."));
                return;
            }

            sendMessage(new Message("ADMIN_ACTION_SUCCESS", "AUCTION_APPROVED", "Approved auction: " + request.getRoomId()));
            broadcastRoomList();
            broadcastPendingAuctionList();
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Auction approval error."));
        }
    }

    private void handleAdminRejectAuction(Message msg) {
        try {
            String requestId = msg.getData() != null ? msg.getData().toString().trim() : "";
            if (!pendingAuctionApprovalService.reject(requestId)) {
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Pending auction request not found."));
                return;
            }

            sendMessage(new Message("ADMIN_ACTION_SUCCESS", "AUCTION_REJECTED", "Rejected auction request: " + requestId));
            broadcastPendingAuctionList();
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Auction rejection error."));
        }
    }

    private void handleAdminDeleteAuction(Message msg) {
        try {
            String targetRoomId = msg.getData() != null ? msg.getData().toString() : "";
            AuctionDAO auctionDAO = new AuctionDAO();

            if (auctionDAO.forceDeleteAuction(targetRoomId)) {
                AuctionStateManager.removeState(targetRoomId);
                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "AUCTION_DELETED", "Force-canceled auction: " + targetRoomId));
                AuctionServer.notifyRoomClosed(targetRoomId);
                broadcastRoomList();
            } else {
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to cancel this auction!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Auction cancellation error!"));
        }
    }

    private void handleAdminDeleteUser(Message msg) {
        try {
            String targetUserId = msg.getData() != null ? msg.getData().toString().trim().toUpperCase() : "";
            UserDAO userDAO = new UserDAO();

            if (userDAO.deleteUser(targetUserId)) {
                sendMessage(new Message("ADMIN_ACTION_SUCCESS", "USER_DELETED", "Da xoa tai khoan: " + targetUserId));

                AuctionServer.notifyDeletedUser(targetUserId);
            } else {
                sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to delete account."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(new Message("ADMIN_ACTION_FAIL", "SERVER", "Account deletion error!"));
        }
    }

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
            AuctionServer.unregisterClient(this);
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

    private void broadcastPendingAuctionList() {
        try {
            AuctionServer.broadcastAll(new Message("ADMIN_PENDING_AUCTION_LIST", "SERVER", pendingAuctionApprovalService.getAllPending()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AuctionRoom buildRoomFromPendingRequest(PendingAuctionRequest request) {
        AuctionRoom room = new AuctionRoom(request.getRoomId(), request.getItemName(), request.getStartingPrice(), request.getSellerId());
        room.setItemId(request.getItemId());
        room.setItemDescription(request.getItemDesc());
        room.setCurrentPrice(request.getStartingPrice());
        room.setStartingPrice(request.getStartingPrice());
        room.setMinimumJoinAmount(request.getMinimumJoinAmount());
        room.setBidStep(request.getBidStep());
        room.setStartTime(request.getStartTime());
        room.setDurationMinutes(request.getDurationMinutes());
        room.setActualEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
        room.setExtensionSeconds(request.getExtensionSeconds());
        room.setSellerReputation(request.getSellerReputation());
        room.setSellerSuccessfulAuctionRate(request.getSuccessfulAuctionRate());
        room.setSellerAdminCancellationRate(request.getAdminCancellationRate());
        room.setStatus(request.getStartTime().isAfter(LocalDateTime.now()) ? "OPEN" : "RUNNING");
        return room;
    }

    private double parseBidAmount(Object data) {
        if (data instanceof Double d) return d;
        if (data instanceof Integer i) return i.doubleValue();
        if (data instanceof Long l) return l.doubleValue();
        return Double.parseDouble(data.toString().trim());
    }

    private void handleGetProductDetails(Message msg) {
        try {
            String roomId = (msg.getData() != null) ? msg.getData().toString().trim() : "";

            if (roomId.isEmpty()) {
                sendMessage(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "Invalid room ID!"));
                return;
            }

            ProductDetailService detailService = new ProductDetailService();
            ProductDetailResponse responseData = detailService.getProductDetails(roomId);

            if (responseData != null) {
                sendMessage(new Message("PRODUCT_DETAILS_SUCCESS", "SERVER", responseData));
            } else {
                sendMessage(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "Auction room details not found!"));
            }

        } catch (Exception e) {
            System.err.println("GET_PRODUCT_DETAILS processing error: " + e.getMessage());
            e.printStackTrace();
            sendMessage(new Message("PRODUCT_DETAILS_FAIL", "SERVER", "System error while loading details!"));
        }
    }

    private String generateItemId() {
        return "IT" + String.format("%05d", System.currentTimeMillis() % 100000);
    }

    private String generateAuctionId() {
        return "AU" + String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    private String generatePendingRequestId() {
        return "PA" + String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}

