package server;

import database.AuthDAO;
import database.ItemDAO;
import database.MockDB;
import database.SellerDAO;
import shared.Message;
import java.io.*;
import java.net.*;
import java.util.*;

public class AuctionServer {
    // Quản lý danh sách các Client đang kết nối
    private static List<ObjectOutputStream> clientWriters = new ArrayList<>();

    // Gọi các DAO ra để tương tác với MockDB
    private static AuthDAO authDAO = new AuthDAO();
    private static ItemDAO itemDAO = new ItemDAO();
    private static SellerDAO sellerDAO = new SellerDAO();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("== SERVER ĐẤU GIÁ ĐÃ KHỞI ĐỘNG TẠI PORT 8888 ==");

        // In sẵn dữ liệu lúc server vừa bật cho sếp soi

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Có client mới kết nối!");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            clientWriters.add(out);

            new Thread(() -> handleClient(in, out)).start();
        }
    }

    private static void handleClient(ObjectInputStream in, ObjectOutputStream out) {
        try {
            while (true) {
                Message msg = (Message) in.readObject();

            }
        } catch (Exception e) {
            clientWriters.remove(out);
        }
    }

    // --- HÀM XỬ LÝ ĐẶT GIÁ (Đồng bộ hóa chống giật lag / race condition) ---
    private static synchronized void processBid(String username, double amount, ObjectOutputStream senderOut) throws Exception {
    }

    // --- HÀM GỬI LẠI TRẠNG THÁI HIỆN TẠI TỪ DB ---
    private static void sendUpdateToClient(ObjectOutputStream out, String maPhien) throws IOException {
    }

}