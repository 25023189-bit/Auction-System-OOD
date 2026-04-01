package com.auction.client.service;

import com.auction.common.dto.Message;
// Đảm bảo đường dẫn này khớp với vị trí file ClientConnection của bạn
import com.auction.client.network.ClientConnection;

/**
 * Lớp AuctionService đóng vai trò là tầng Dịch vụ (Service Layer) của Client.
 * Nhiệm vụ chính: Cung cấp các phương thức giao tiếp rõ ràng cho UI Controller gọi.
 */
public class AuctionService {
    // Đối tượng quản lý kết nối Socket thực tế
    private ClientConnection clientConnection;

    // Lưu trữ thông tin định danh của người dùng hiện tại (Mã BD5xxxxx)
    private String currentUser = "";

    /**
     * Khởi tạo service với một kết nối đã có sẵn.
     */
    public AuctionService(ClientConnection connection) {
        this.clientConnection = connection;
    }

    // --- GETTER / SETTER ---
    public void setCurrentUser(String userid) {
        this.currentUser = userid;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    // --- CÁC NGHIỆP VỤ GỬI YÊU CẦU LÊN SERVER ---

    /**
     * Gửi yêu cầu Đăng nhập.
     */
    public void login(String id, String password) {
        if (clientConnection != null) {
            // Sử dụng Constructor: Message(String action, String id, Object data)
            clientConnection.sendMessage(new Message("LOGIN", id, (Object) password));
        }
    }

    /**
     * Gửi yêu cầu Đăng ký tài khoản mới.
     */
    public void register(String role, String username, String password) {
        if (clientConnection != null) {
            // Sử dụng Constructor 5 tham số: action, id, username, role, data
            clientConnection.sendMessage(new Message("REGISTER", "NEW", username, role, (Object) password));
        }
    }

    /**
     * Gửi yêu cầu tham gia vào một phòng đấu giá cụ thể.
     */
    public void joinRoom(String roomId) {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("JOIN_ROOM", roomId, (Object) currentUser));
        }
    }

    /**
     * Gửi yêu cầu rời khỏi phòng đấu giá hiện tại.
     */
    public void leaveRoom() {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("LEAVE_ROOM", currentUser, (Object) ""));
        }
    }

    /**
     * Gửi yêu cầu đặt giá (Bid) cho vật phẩm.
     */
    public void placeBid(double amount) {
        if (clientConnection != null) {
            // Sử dụng Constructor: Message(String action, Object data, String role)
            // Truyền currentUser vào tham số cuối (role/sender) để server biết ai đặt giá
            clientConnection.sendMessage(new Message("BID", (Object) amount, currentUser));
        }
    }

    /**
     * Gửi tin nhắn chat vào phòng.
     */
    public void sendChat(String chatContent) {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("CHAT_MSG", (Object) chatContent, currentUser));
        }
    }

    /**
     * Gửi yêu cầu lấy lại mật khẩu.
     */
    public void resetPassword(String username, String newPassword){
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("RESET_PASSWORD", username, (Object) newPassword));
        }
    }

    /**
     * (Nghiệp vụ Seller) Gửi yêu cầu tạo phòng đấu giá mới.
     */
    public void createAuction(String itemName, double startingPrice) {
        if (clientConnection != null) {
            // Gói dữ liệu vào chuỗi payload để Server dễ xử lý
            String payload = itemName + "|" + startingPrice;
            clientConnection.sendMessage(new Message("CREATE_AUCTION", currentUser, (Object) payload));
        }
    }

    /**
     * Gửi yêu cầu lấy danh sách phòng.
     */
    public void getRooms() {
        if (clientConnection != null) {
            clientConnection.sendMessage(new Message("GET_ROOMS", currentUser, (Object) ""));
        }
    }
}