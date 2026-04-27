package com.auction.server.main;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.ClientHandler;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionRoomService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {

    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // NÂNG CẤP 1: Map lưu trữ UserID và Handler để gửi tin nhắn riêng
    public static Map<String, ClientHandler> userSessions = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService AUCTION_WATCHER = Executors.newSingleThreadScheduledExecutor();

    // NÂNG CẤP 2: Giới hạn tối đa 100 kết nối cùng lúc bằng ThreadPool
    private static final ExecutorService CLIENT_POOL = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        int port = 8080;

        // NÂNG CẤP 3: Xử lý khi tắt Server an toàn (Graceful Shutdown)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Initiating graceful shutdown...");
            broadcast(new Message("SERVER_SHUTDOWN", "SERVER", "System under maintenance"));
            if (AUCTION_WATCHER != null) AUCTION_WATCHER.shutdown();
            if (CLIENT_POOL != null) CLIENT_POOL.shutdown();
            System.out.println("[Server] Shutdown complete.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            startExpiredAuctionWatcher();
            System.out.println("Server running on port: " + port);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(socket);
                addClient(handler);

                // Dùng ThreadPool thay vì new Thread() để tối ưu hiệu năng
                CLIENT_POOL.execute(handler);
            }

        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void broadcast(Message msg) {
        System.out.println("Broadcast: " + msg.getAction());

        for (ClientHandler client : clients) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    public static void broadcastToRoom(String roomId, Message msg) {
        for (ClientHandler client : clients) {
            if (client == null || !client.isAlive()) {
                removeClient(client);
                continue;
            }

            String clientRoom = client.getCurrentRoomId();
            if (roomId != null && roomId.equals(clientRoom)) {
                client.sendMessage(msg);
            }
        }
    }

    public static void notifyRoomClosed(String roomId) {
        Message message = new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId);
        for (ClientHandler client : clients) {
            if (client == null || !client.isAlive()) {
                removeClient(client);
                continue;
            }

            if (roomId != null && roomId.equals(client.getCurrentRoomId())) {
                client.sendMessage(message);
                client.leaveCurrentRoomIfMatches(roomId);
            }
        }
    }

    // NÂNG CẤP 4: Hàm gửi tin nhắn riêng cho 1 user
    public static void sendToUser(String userId, Message msg) {
        ClientHandler client = userSessions.get(userId);
        if (client != null && client.isAlive()) {
            client.sendMessage(msg);
        } else {
            System.out.println("User " + userId + " is offline or does not exist.");
        }
    }

    public static void addClient(ClientHandler client) {
        if (client != null) {
            clients.add(client);
            System.out.println("Client connected | Online: " + clients.size());
        }
    }

    public static void removeClient(ClientHandler client) {
        if (client != null) {
            clients.remove(client);
            if (client.getUserId() != null) {
                userSessions.remove(client.getUserId());
            }
            client.closeConnection();
            System.out.println("Client disconnected | Online: " + clients.size());
        }
    }

    private static void startExpiredAuctionWatcher() {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoomService roomService = new AuctionRoomService();

        AUCTION_WATCHER.scheduleAtFixedRate(() -> {
            try {
                List<AuctionRoom> activeRooms = auctionDAO.getAllActiveAuctions();
                for (AuctionRoom room : activeRooms) {
                    if (room != null && room.getRoomId() != null) {
                        roomService.finalizeExpiredAuctionIfNeeded(room.getRoomId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Auction watcher error: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void notifyDeletedUser(String userId) {
        // Tìm user trong danh sách đang online
        ClientHandler client = userSessions.get(userId);

        if (client != null && client.isAlive()) {
            // Gửi thông báo cho Client biết họ đã bị ban (nhưng nội dung tin nhắn vẫn là tiếng Anh)
            client.sendMessage(new Message("BANNED", "SERVER", "Your account has been deleted by the Admin!"));
            // Ngắt kết nối ngay lập tức
            client.closeConnection();
        }
    }
}