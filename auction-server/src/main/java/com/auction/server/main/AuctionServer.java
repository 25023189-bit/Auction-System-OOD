package com.auction.server.main;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.handler.ClientHandler;
import com.auction.server.service.AuctionRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Entry point của socket server cho hệ thống đấu giá.
 *
 * Vai trò:
 * - Lắng nghe client trên cổng server, tạo ClientHandler và quản lý danh sách client online.
 * - Broadcast message, notify theo room/user và chạy watcher chốt phiên hết thời gian.
 *
 * Luồng chính:
 * 1. Khởi tạo shutdown hook, ServerSocket, watcher và thread pool xử lý client.
 * 2. Chấp nhận socket mới, đưa vào danh sách client và giao ClientHandler cho CLIENT_POOL.
 *
 * Business rules:
 * - Phiên hết giờ phải được watcher kiểm tra định kỳ và finalize qua AuctionRoomService.
 * - User bị admin xóa khi đang online phải nhận BANNED rồi bị ngắt kết nối.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: dùng CopyOnWriteArrayList, ConcurrentHashMap và executor; method static vẫn cần cẩn trọng khi gọi chéo close/remove.
 * - Dependency: ServerSocket, ClientHandler, AuctionDAO, AuctionRoomService, Message, java.util.concurrent, SLF4J.
 */
public class AuctionServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionServer.class);

    // CopyOnWriteArrayList phù hợp cho broadcast vì số lần duyệt nhiều hơn số lần thêm/xóa client.
    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // Map userId -> handler để server gửi thông báo riêng, ví dụ tài khoản bị admin xóa.
    public static Map<String, ClientHandler> userSessions = new ConcurrentHashMap<>();

    // Watcher chạy nền để tự finalize các phiên đã quá giờ.
    private static final ScheduledExecutorService AUCTION_WATCHER = Executors.newSingleThreadScheduledExecutor();

    // Thread pool giới hạn số client xử lý đồng thời thay vì tạo thread không kiểm soát.
    private static final ExecutorService CLIENT_POOL = Executors.newFixedThreadPool(100);

    private static final int  port = 8080;

    public static void main(String[] args) {

        // Graceful shutdown: thông báo client và dừng các executor khi server tắt.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Initiating graceful shutdown.");
            broadcast(new Message("SERVER_SHUTDOWN", "SERVER", "System under maintenance"));
            if (AUCTION_WATCHER != null) AUCTION_WATCHER.shutdown();
            if (CLIENT_POOL != null) CLIENT_POOL.shutdown();
            LOGGER.info("Shutdown complete.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            startExpiredAuctionWatcher();
            LOGGER.info("Server running on port: {}", port);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(socket);
                addClient(handler);

                // Mỗi socket được xử lý bởi một ClientHandler trong thread pool.
                CLIENT_POOL.execute(handler);
            }

        } catch (Exception e) {
            LOGGER.error("Server error.", e);
        }
    }

    public static void broadcast(Message msg) {
        LOGGER.debug("Broadcast: {}", msg.getAction());

        // Gửi tới mọi client còn sống, đồng thời dọn client đã mất kết nối.
        for (ClientHandler client : clients) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    public static void broadcastToRoom(String roomId, Message msg) {
        // Chỉ gửi message cho client đang ở đúng phòng đấu giá.
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
        // Thông báo cho client trong phòng và xóa trạng thái room của họ.
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

    public static void addClient(ClientHandler client) {
        if (client != null) {
            clients.add(client);
            LOGGER.info("Client connected | Online: {}", clients.size());
        }
    }

    public static void removeClient(ClientHandler client) {
        if (client != null) {
            clients.remove(client);
            if (client.getUserId() != null) {
                userSessions.remove(client.getUserId());
            }
            client.closeConnection();
            LOGGER.info("Client disconnected | Online: {}", clients.size());
        }
    }

    private static void startExpiredAuctionWatcher() {
        // Dùng interface để server không phụ thuộc trực tiếp vào implementation của DAO.
        com.auction.server.dao.IAuctionDAO auctionDAO = new com.auction.server.dao.AuctionDAO();
        AuctionRoomService roomService = new AuctionRoomService();

        AUCTION_WATCHER.scheduleAtFixedRate(() -> {
            try {
                // Mỗi giây quét các phiên đang mở để đóng những phiên đã hết thời gian.
                List<AuctionRoom> activeRooms = auctionDAO.getAllOpenOrRunningAuctions();
                for (AuctionRoom room : activeRooms) {
                    if (room != null && room.getRoomId() != null) {
                        roomService.finalizeExpiredAuctionIfNeeded(room.getRoomId());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Auction watcher error.", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    public static void notifyDeletedUser(String userId) {
        // Tìm user trong danh sách đang online.
        ClientHandler client = userSessions.get(userId);

        if (client != null && client.isAlive()) {
            // Gửi thông báo rồi ngắt kết nối để user bị xóa không tiếp tục thao tác.
            client.sendMessage(new Message("BANNED", "SERVER", "Your account has been deleted by the Admin!"));
            client.closeConnection();
        }
    }
}
