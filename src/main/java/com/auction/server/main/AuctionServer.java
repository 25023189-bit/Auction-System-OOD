package com.auction.server.main;

// SỬA: Import đúng vị trí của ClientHandler và Message
import com.auction.common.dto.Message;
import com.auction.server.ClientHandler;
import java.util.concurrent.CopyOnWriteArrayList;

import java.net.*;
import java.util.*;

/**
 * Lớp AuctionServer là điểm khởi chạy (Entry Point) của toàn bộ hệ thống Backend.
 * Nhiệm vụ chính:
 * 1. Mở cổng mạng (Port) và liên tục lắng nghe các kết nối từ Client.
 * 2. Mỗi khi có 1 Client kết nối, tạo một luồng (Thread) mới để phục vụ riêng cho Client đó.
 * 3. Quản lý danh sách tập trung toàn bộ các Client đang online để phục vụ việc Broadcast (phát sóng).
 */

public class AuctionServer {

    // Danh sách lưu trữ tất cả các Client đang kết nối tới Server.
    // LƯU Ý:
    // Hiện tại đang dùng ArrayList cơ bản. Nếu dự án có hàng ngàn user cùng lúc,
    // hãy cân nhắc đổi sang CopyOnWriteArrayList hoặc dùng Collections.synchronizedList
    // để tránh lỗi ConcurrentModificationException khi có người vào/ra liên tục.

    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 8080; // Cổng mặc định của Server
        try {
            // Mở cổng (Mở "cửa hàng" để đón khách)
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server active success on port: " + port);

            // Vòng lặp vô tận: Liên tục chờ đón các Client mới
            while (true) {
                // Lệnh accept() sẽ block (chặn) tại đây cho đến khi có 1 Client kết nối vào
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(socket);
                addClient(handler);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            System.out.println("Error Port.");
            e.printStackTrace();
        }
    }

    // ==========================================================
    // CÁC HÀM TIỆN ÍCH PHÁT SÓNG (BROADCAST)
    // ==========================================================

    /**
     * Gửi cho tất cả mọi người.
     */
    public static void broadcast(Message msg) {
        System.out.println("Broadcasting action: " + msg.getAction());
        for (ClientHandler client : clients) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    /**
     * Gửi tin nhắn cho tất cả mọi người đang online trên Server.
     * Thường dùng cho các thông báo hệ thống (VD: "Server sẽ bảo trì sau 5 phút").
     */
    public static void broadcastAll(Message msg) {
        for (ClientHandler client : clients) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    /**
     * Chỉ gửi tin nhắn cho những người đang ở trong một phòng đấu giá cụ thể.
     * Lưu ý: Vẫn chưa phát triển xong
     * Dùng khi có người đặt giá mới (Bid) hoặc chat trong phòng.
     * @param roomId Mã phòng cần gửi
     * @param msg Gói tin chứa nội dung.
     */
    public static void broadcastToRoom(String roomId, Message msg) {
        for (ClientHandler client : clients) {
            if (client == null || !client.isAlive()) {
                removeClient(client);
                continue;
            }

            if (roomId != null && roomId.equals(client.getCurrentRoomId())) {
                client.sendMessage(msg);
            }
        }
    }

    public static void addClient(ClientHandler client) {
        if (client != null) {
            clients.add(client);
            System.out.println("✅ Client connected. Online: " + clients.size());
        }
    }

    public static void removeClient(ClientHandler client) {
        if (client != null) {
            clients.remove(client);
            System.out.println("❌ Client disconnected. Online: " + clients.size());
        }
    }
}