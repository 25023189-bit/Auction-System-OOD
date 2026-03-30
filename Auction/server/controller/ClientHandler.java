package server.controller;

import common.DTO.Message;
import common.models.Auctions.AuctionRoom;
import server.database.MockDB;
import server.main.AuctionServer;
import server.services.AuthService;
import server.services.AuctionRoomService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Lớp ClientHandler chịu trách nhiệm duy trì kết nối 1-1 với một Client cụ thể.
 * Implement Runnable để mỗi Client kết nối đến sẽ được chạy trên một luồng (Thread) độc lập,
 * giúp Server có thể phục vụ hàng ngàn Client cùng lúc mà không bị nghẽn.
 * * Kiến trúc: Router/Controller - Nhận request từ Client -> Phân loại action -> Gọi Service xử lý -> Trả kết quả.
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
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Error creating a communication flow with the Client!");
            e.printStackTrace();
        }
    }

    public String getCurrentRoomId() { return currentRoomId; }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                switch (msg.action) {
                    case "LOGIN":
                        sendMessage(authService.login(msg.id, (String) msg.data));
                        break;

                    case "REGISTER":
                        String regUser = msg.id;

                        String[] regData = ((String) msg.data).split("\\|");

                        String regPass = regData[0];
                        String regRole = regData[1];

                        Message regResponse = authService.register(regUser, regPass, regRole);

                        sendMessage(regResponse);
                        break;

                    case "JOIN_ROOM":
                        currentRoomId = (String) msg.data;
                        sendMessage(roomService.joinRoom(currentRoomId));
                        break;

                    case "LEAVE_ROOM":
                        currentRoomId = "";
                        break;

                    case "BID":
                        if (currentRoomId.isEmpty()) break;

                        Message bidResult = roomService.processBid(currentRoomId, msg.id, (double) msg.data);

                        if (bidResult.action.equals("NEW_BID")) {
                            AuctionServer.broadcastToRoom(currentRoomId, bidResult);
                        } else {
                            sendMessage(bidResult);
                        }
                        break;

                    case "CHAT_MSG":
                        AuctionServer.broadcastAll(new Message("CHAT_MSG", msg.id , msg.data));
                        break;
                    case "RESET_PASSWORD":
                        Message resetResult = authService.resetPassword(msg.id, (String) msg.data);

                        out.writeObject(resetResult);
                        out.flush();
                        break;
                    case "CREATE_AUCTION":
                        // 1. Lấy thông tin từ gói tin gửi lên
                        String sellerName = msg.id; // Tên người bán
                        String[] auctionData = ((String) msg.data).split("\\|"); // Tách dữ liệu bằng dấu |

                        String itemName = auctionData[0];
                        double startingPrice = Double.parseDouble(auctionData[1]);

                        // 2. Đi qua trạm kiểm duyệt trung gian
                        AuctionRoomService roomService = new AuctionRoomService();
                        boolean isApproved = roomService.validateAuctionItem(msg.id, itemName, startingPrice);
                        if (isApproved) {
                            String newRoomId = "AU" + System.currentTimeMillis();
                            AuctionRoom newRoom = new AuctionRoom(newRoomId, itemName, startingPrice);

                            // Lưu vào Database
                            MockDB.auctionTable.put(newRoomId, newRoom);

                            // 1. Báo cho Seller biết để Seller tự động chui vào phòng
                            sendMessage(new Message("CREATE_AUCTION_SUCCESS", newRoomId, "Tạo thành công"));

                            // 2. Gom toàn bộ danh sách phòng mới nhất ngay tại Server
                            StringBuilder roomsInfo = new StringBuilder();
                            for (AuctionRoom room : MockDB.auctionTable.values()) {
                                roomsInfo.append(room.getRoomId()).append("|")
                                        .append(room.getItemName()).append("|")
                                        .append(room.getCurrentPrice()).append(";");
                            }

                            // 3. Bắn thẳng danh sách (ROOM_LIST) cho TẤT CẢ mọi người , không dùng UPDATE_ROOMS nữa!
                            AuctionServer.broadcast(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));

                        } else {
                            sendMessage(new Message("CREATE_AUCTION_FAIL", "SERVER", "Sản phẩm bị từ chối kiểm duyệt!"));
                        }
                        break;
                    case "GET_ROOMS":
                        // Lục lọi trong kho MockDB xem có bao nhiêu phòng thì gom hết lại
                        StringBuilder roomsInfo = new StringBuilder();

                        for (AuctionRoom room : MockDB.auctionTable.values()) {
                            // Đóng gói theo chuẩn: Mã phòng|Tên sản phẩm|Giá hiện tại;
                            roomsInfo.append(room.getRoomId()).append("|")
                                    .append(room.getItemName()).append("|")
                                    .append(room.getCurrentPrice()).append(";");
                        }

                        // Gửi nguyên 1 cục chuỗi này về cho Client
                        sendMessage(new Message("ROOM_LIST", "SERVER", roomsInfo.toString()));
                        break;
                }
            }
        } catch (Exception e) {
            // Xử lý ngắt kết nối
        }
    }

    /**
     * Gửi một thông điệp từ Server về cho Client này.
     * @param response Đối tượng Message chứa kết quả xử lý
     */
    public void sendMessage(Message response) {
        try {
            out.writeObject(response);
            out.flush();
            out.reset();
        } catch (Exception e) {
            System.out.println("Error sending response to Client!");
        }
    }
}