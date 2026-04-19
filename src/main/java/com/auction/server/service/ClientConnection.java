package com.auction.server.service;

import com.auction.client.feature.controllers.AuctionController;
import com.auction.common.dto.Message;
import java.io.*;
import java.net.Socket;

/**
 * Lớp ClientConnection quản lý kết nối mạng TCP/IP giữa Client và Server.
 * Chịu trách nhiệm thiết lập Socket, gửi dữ liệu (writeObject) và
 * liên tục lắng nghe dữ liệu trả về trên một Thread riêng biệt.
 * * TODO cho người phát triển sau: Nếu sau này dự án scale lên,
 * có thể cân nhắc chuyển tham chiếu 'AuctionController' thành một Interface (Listener)
 * để giảm sự phụ thuộc cứng (tight-coupling) giữa mạng và giao diện.
 */
public class ClientConnection {
    // Định danh user hiện tại lưu ở mức static (Cân nhắc gộp chung vào AuctionService để quản lý)
    public static String currentUser = null;

    // Cấu hình mạng
    private final String host = "localhost"; // Đổi thành IP Server thật khi deploy
    private int port = 8080;

    // Các đối tượng cốt lõi của Socket và Stream
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Giữ tham chiếu ngược lại Controller để trả data về UI sau khi nhận được từ Server
    private AuctionController controller;

    /**
     * Constructor gắn kết nối với giao diện chính (Controller).
     */
    public ClientConnection(AuctionController controller) {
        this.controller = controller;
    }

    /**
     * Bắt đầu tiến trình kết nối tới Server.
     * Phải chạy trên một luồng riêng (Thread) để vòng lặp while(true)
     * không làm đơ (block) giao diện JavaFX.
     */
    public void connect() {
        new Thread(() -> {
            try {
                // Cập nhật UI: Đang kết nối
                controller.updateConnectionStatus("Đang kết nối...");

                // Khởi tạo Socket và luồng dữ liệu Object (Lưu ý: Khởi tạo out trước in để tránh deadlock ở Server)
                socket = new Socket(host, port);
                // Quan trọng: Khởi tạo OutputStream trước InputStream
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Cập nhật UI: Kết nối thành công
                controller.updateConnectionStatus("Đã kết nối Server!");

                // VÒNG LẶP LẮNG NGHE LẼ BẤT TẬN (Infinite Listener Loop)
                // Liên tục chờ và đọc các đối tượng Message do Server đẩy về
                while (true) {
                    Message response = (Message) in.readObject();
                    System.out.println("📥 Client nhận được action: " + response.getAction());

                    // Chuyển tiếp Message nhận được về cho Controller xử lý hiển thị lên UI
                    // Ghi chú: Bên trong hàm onServerResponse của Controller phải dùng Platform.runLater()
                    controller.onServerResponse(response);
                }
            } catch (java.net.SocketException se) {
                if (se.getMessage() != null && se.getMessage().toLowerCase().contains("socket closed")) {
                    System.out.println("ℹ️ Kết nối mạng đã được thu hồi an toàn (Người dùng đã ngắt kết nối).");
                } else {
                    System.err.println("❌ Mất kết nối tới Server: " + se.getMessage());
                }
            } catch (java.io.EOFException eof) {
                System.out.println("ℹ️ Server đã chủ động ngắt kết nối.");
            } catch (Exception e) {
                System.err.println("❌ Lỗi luồng đọc dữ liệu: " + e.getMessage());
                // e.printStackTrace();
            }
        }).start();
    }

    /**
     * Hàm gửi đối tượng Message lên Server.
     * Hàm này được gọi bởi AuctionService.
     * @param msg Đối tượng chuẩn giao tiếp giữa Client và Server
     */
    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
                out.reset();
            } else {
                System.err.println("⚠️ Cảnh báo: ObjectOutputStream chưa được khởi tạo!");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi tin nhắn lên Server: ");
            e.printStackTrace();
        }
    }

    //Sử dụng trong các tình huống khẩn cấp
    public void closeConnection() {
        try {
            if (this.in != null) {
                this.in.close();
            }

            if (this.out != null) {
                this.out.close();
            }

            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }

            System.out.println("🔌 Đã ngắt kết nối với Server an toàn.");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cố gắng ngắt kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}