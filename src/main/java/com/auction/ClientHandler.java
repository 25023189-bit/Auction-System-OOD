package com.auction.server; // Đã sửa: Đưa về đúng package com.auction.server

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.dao.MockDB;
import com.auction.server.main.AuctionServer;
import com.auction.server.service.AuctionRoomService;
import com.auction.server.service.AuthService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Lớp ClientHandler chịu trách nhiệm duy trì kết nối 1-1 với một Client cụ thể.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String currentRoomId = "";

    private AuthService authService = new AuthService();
    private AuctionRoomService roomService = new AuctionRoomService();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            // Lưu ý: Khởi tạo Output trước Input để tránh deadlock
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khởi tạo luồng giao tiếp với Client!");
            e.printStackTrace();
        }
    }

    public String getCurrentRoomId() { return currentRoomId; }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (!(obj instanceof Message)) continue;

                Message msg = (Message) obj;
                String action = msg.getAction();

                switch (action) {
                    case "LOGIN":
                        sendMessage(authService.login(msg.getId(), (String) msg.getData()));
                        break;

                    case "REGISTER":
                        // Dữ liệu đăng ký gửi lên dạng: password|role
                        String[] regData = ((String) msg.getData()).split("\\|");
                        if (regData.length >= 2) {
                            sendMessage(authService.register(msg.getUsername(), regData[0], regData[1]));
                        }
                        break;

                    case "JOIN_ROOM":
                        currentRoomId = (String) msg.getData();
                        sendMessage(roomService.joinRoom(currentRoomId));
                        break;

                    case "LEAVE_ROOM":
                        currentRoomId = "";
                        break;

                    case "BID":
                        if (!currentRoomId.isEmpty()) {
                            // Ép kiểu data về double để tính toán giá
                            double amount = (msg.getData() instanceof Double) ? (Double) msg.getData() : Double.parseDouble(msg.getData().toString());
                            Message bidResult = roomService.processBid(currentRoomId, msg.getId(), amount);

                            if ("NEW_BID".equals(bidResult.getAction())) {
                                AuctionServer.broadcastToRoom(currentRoomId, bidResult);
                            } else {
                                sendMessage(bidResult);
                            }
                        }
                        break;

                    case "CHAT_MSG":
                        // Gửi tin nhắn cho tất cả mọi người online
                        AuctionServer.broadcastAll(new Message("CHAT_MSG", (Object) msg.getData(), msg.getId()));
                        break;

                    case "RESET_PASSWORD":
                        sendMessage(authService.resetPassword(msg.getId(), (String) msg.getData()));
                        break;

                    case "CREATE_AUCTION":
                        String[] auctionData = ((String) msg.getData()).split("\\|");
                        if (auctionData.length >= 2) {
                            String itemName = auctionData[0];
                            double startingPrice = Double.parseDouble(auctionData[1]);

                            if (roomService.validateAuctionItem(msg.getId(), itemName, startingPrice)) {
                                String newRoomId = "AU" + System.currentTimeMillis();
                                AuctionRoom newRoom = new AuctionRoom(newRoomId, itemName, startingPrice);
                                MockDB.auctionTable.put(newRoomId, newRoom);

                                // Phản hồi cho người tạo
                                sendMessage(new Message("CREATE_AUCTION_SUCCESS", newRoomId, (Object) "Tạo thành công"));

                                // Cập nhật danh sách phòng cho tất cả mọi người
                                broadcastRoomList();
                            } else {
                                sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", (Object) "Sản phẩm bị từ chối!"));
                            }
                        }
                        break;

                    case "GET_ROOMS":
                        sendRoomList();
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Client đã ngắt kết nối.");
        } finally {
            closeConnection();
        }
    }

    private void sendRoomList() {
        sendMessage(new Message("ROOM_LIST", "SERVER", (Object) buildRoomData()));
    }

    private void broadcastRoomList() {
        AuctionServer.broadcast(new Message("ROOM_LIST", "SERVER", (Object) buildRoomData()));
    }

    private String buildRoomData() {
        StringBuilder sb = new StringBuilder();
        for (AuctionRoom room : MockDB.auctionTable.values()) {
            sb.append(room.getRoomId()).append("|")
                    .append(room.getItemName()).append("|")
                    .append(room.getCurrentPrice()).append(";");
        }
        return sb.toString();
    }

    public void sendMessage(Message response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset(); // Quan trọng: Xóa cache Object để gửi dữ liệu mới
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi response: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            AuctionServer.clients.remove(this);
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}