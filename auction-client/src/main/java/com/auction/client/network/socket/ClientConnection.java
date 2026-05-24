package com.auction.client.network.socket;

import com.auction.common.dto.Message;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kết nối socket phía client tới AuctionServer.
 *
 * Vai trò:
 * - Mở socket, tạo stream object và gửi Message từ client lên server.
 * - Chạy reader thread nhận response liên tục rồi chuyển về AuctionController.
 *
 * Luồng chính:
 * 1. connect() tạo thread nền, kết nối localhost:8080 và khởi tạo ObjectInputStream/ObjectOutputStream.
 * 2. Reader loop đọc Message từ server, gọi controller.onServerResponse(), còn sendMessage() ghi request ra socket.
 *
 * Business rules:
 * - UI phải được cập nhật trạng thái kết nối khi bắt đầu và sau khi connect thành công.
 * - Khi disconnect phải đóng input, output và socket để reader loop kết thúc sạch.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe đầy đủ: socket/stream được dùng giữa UI thread và reader thread nhưng không có synchronized.
 * - Dependency: ServerMessageListener, Socket, ObjectInputStream/ObjectOutputStream, Message, SLF4J.
 */
public class ClientConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class);

    private final String host = "100.120.197.41";
    private final int port = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final ServerMessageListener listener;

    public ClientConnection(ServerMessageListener listener) {
        this.listener = listener;
    }

    public void connect() {
        new Thread(() -> {
            try {
                listener.updateConnectionStatus("Connecting...");

                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                listener.updateConnectionStatus("Connected to server!");

                // Reader loop nhận Message từ server cho đến khi socket đóng.
                while (!Thread.currentThread().isInterrupted() && socket != null && !socket.isClosed()) {
                    Message response = (Message) in.readObject();
                    LOGGER.debug("Received action: {}", response.getAction());
                    listener.onServerResponse(response);
                }
            } catch (java.net.SocketException se) {
                if (se.getMessage() != null && se.getMessage().toLowerCase().contains("socket closed")) {
                    LOGGER.info("Network connection closed safely.");
                } else {
                    LOGGER.warn("Lost connection to server: {}", se.getMessage());
                }
            } catch (EOFException eof) {
                LOGGER.info("Server closed the connection.");
            } catch (Exception e) {
                LOGGER.error("Reader thread error.", e);
            }
        }).start();
    }

    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                // reset() tránh gửi lại object cũ khi cùng Message instance được tái sử dụng.
                out.writeObject(msg);
                out.flush();
                out.reset();
            } else {
                LOGGER.warn("ObjectOutputStream is not initialized.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to send message to server.", e);
        }
    }

    public void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            LOGGER.info("Disconnected from server safely.");
        } catch (Exception e) {
            LOGGER.error("Failed to close connection.", e);
        }
    }
}
