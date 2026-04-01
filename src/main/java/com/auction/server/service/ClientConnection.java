package com.auction.client.network; // ĐÃ SỬA: Đưa về đúng thư mục client.network

import com.auction.client.controllers.AuctionController;
import com.auction.common.dto.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Lớp ClientConnection quản lý kết nối mạng TCP/IP giữa Client và Server.
 */
public class ClientConnection {
    public static String currentUser = null;
    private final String host = "localhost";
    private int port = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private AuctionController controller;

    public ClientConnection(AuctionController controller) {
        this.controller = controller;
    }

    public void connect() {
        new Thread(() -> {
            try {
                if (controller != null) controller.updateConnectionStatus("Đang kết nối...");

                socket = new Socket(host, port);
                // Quan trọng: Khởi tạo OutputStream trước InputStream
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                if (controller != null) controller.updateConnectionStatus("Đã kết nối Server!");

                while (!socket.isClosed()) {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message response = (Message) obj;
                        System.out.println("📥 Client nhận: " + response.getAction());
                        if (controller != null) controller.onServerResponse(response);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi kết nối Socket: " + e.getMessage());
                if (controller != null) controller.updateConnectionStatus("Mất kết nối!");
            }
        }).start();
    }

    /**
     * Hàm gửi đối tượng Message lên Server.
     */
    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
                out.reset(); // Tránh lỗi cache khi gửi cùng 1 object nhiều lần
            } else {
                System.err.println("⚠️ Cảnh báo: Stream chưa sẵn sàng!");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi tin nhắn: " + e.getMessage());
        }
    }
}