package client.services;

import common.DTO.Message;

/**
 * Lớp AuctionService đóng vai trò là tầng Dịch vụ (Service Layer) của Client.
 * Nhiệm vụ chính: Cung cấp các phương thức giao tiếp rõ ràng cho UI Controller gọi.
 * Thay vì Controller phải tự đóng gói Message và gửi đi, nó chỉ cần gọi hàm ở đây.
 */
public class AuctionService {
    // Đối tượng quản lý kết nối Socket thực tế
    private ClientConnection clientConnection;

    // Lưu trữ thông tin định danh của người dùng hiện tại sau khi đăng nhập thành công
    private String currentUser = "";
    private String currentId = "";

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
     * id Tên đăng nhập
     * password Mật khẩu
     */
    public void login(String id, String password) {
        clientConnection.sendMessage(new Message("LOGIN", id, password));
    }

    /**
     * Gửi yêu cầu Đăng ký tài khoản mới.
     * @param role Vai trò (VD: "Bidder", "Seller")
     * @param username Tên hiển thị/tài khoản
     * @param password Mật khẩu
     */
    public void register(String role, String username, String password) {
        clientConnection.sendMessage(new Message("REGISTER", role, username+"|"+password));
    }

    /**
     * Gửi yêu cầu tham gia vào một phòng đấu giá cụ thể.
     * @param roomId Mã phòng đấu giá
     */
    public void joinRoom(String roomId) {
        clientConnection.sendMessage(new Message("JOIN_ROOM", currentUser,roomId  ));
    }

    /**
     * Gửi yêu cầu rời khỏi phòng đấu giá hiện tại.
     */
    public void leaveRoom() {
        clientConnection.sendMessage(new Message("LEAVE_ROOM", currentUser, ""));
    }

    /**
     * Gửi yêu cầu đặt giá (Bid) cho vật phẩm trong phòng.
     * @param amount Số tiền đặt cược
     */
    public void placeBid(double amount) {
        clientConnection.sendMessage(new Message("BID", currentUser, amount));
    }

    /**
     * Gửi tin nhắn chat vào phòng đấu giá.
     * @param chatContent Nội dung chat
     */
    public void sendChat(String chatContent) {
        Message msg = new Message("CHAT_MSG", ClientConnection.currentUser, chatContent);
        clientConnection.sendMessage(msg);
    }

    /**
     * Gửi yêu cầu lấy lại mật khẩu.
     */
    public void resetPassword(String username, String newPassword){
        clientConnection.sendMessage(new Message("RESET_PASSWORD", username, newPassword));
    }

    /**
     * (Nghiệp vụ Seller) Gửi yêu cầu tạo phòng đấu giá mới lên Server.
     * @param itemName Tên vật phẩm
     * @param startingPrice Giá khởi điểm
     */
    public void createAuction(String itemName, double startingPrice) {
        String payload = itemName + "|" + startingPrice;
        clientConnection.sendMessage(new Message("CREATE_AUCTION", currentUser, payload));
    }

    /**
     * Gửi yêu cầu lấy danh sách toàn bộ các phòng đấu giá đang hoạt động.
     */
    public void getRooms() {
        clientConnection.sendMessage(new Message("GET_ROOMS", currentUser, ""));
    }
}
