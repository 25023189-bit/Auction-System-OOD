package client.network;

import shared.Message;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerConnection {
    private static ServerConnection instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<Message> messageHandler; // Hàm callback để báo cho Controller khi có thư mới
    public void setMessageHandler(Consumer<Message> handler) { this.messageHandler = handler; }

    private ServerConnection() {}

    public static ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    public void connect(String host, int port, Consumer<Message> onMessageReceived) {
        this.messageHandler = onMessageReceived;
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Message msg = (Message) in.readObject();
                    if (messageHandler != null) messageHandler.accept(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void send(shared.Message msg) {
        // Thêm đoạn check null này vào đầu hàm
        if (this.out == null) {
            System.out.println("⚠️ CẢNH BÁO: Chưa kết nối được tới Server! Nút gửi bị liệt tạm thời.");
            return; // Dừng lại luôn, không chạy đoạn gửi nữa
        }

        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}