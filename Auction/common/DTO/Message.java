package common.DTO;

import java.io.Serializable;

/**
 * Lớp Message đóng vai trò là Đối tượng truyền tải dữ liệu (Data Transfer Object - DTO).
 * Đây là "Ngôn ngữ chung" (Giao thức) duy nhất để Client và Server nói chuyện với nhau qua Socket.
 * * ⚠️ QUAN TRỌNG CHO TEAM PHÁT TRIỂN:
 * Lớp này BẮT BUỘC phải implements Serializable để Java có thể chuyển object này
 * thành chuỗi byte (mã hóa) truyền qua mạng, và giải mã lại ở đầu nhận.
 * Mọi object được nhét vào biến `data` bên dưới CŨNG PHẢI implements Serializable
 * (ví dụ: String, Integer, hoặc các Model như AuctionRoom, User).
 */

public class Message implements Serializable {
    // Đảm bảo tính tương thích phiên bản khi truyền qua mạng.
    // Tránh lỗi InvalidClassException nếu Server và Client dùng 2 bản code lệch nhau.
    private static final long serialVersionUID = 1L;

    /**
     * Hành động cần thực hiện. Đóng vai trò như URL/Endpoint trong API.
     * Ví dụ: "LOGIN", "CREATE_AUCTION", "ROOM_LIST", "BID", "CHAT_MSG"...
     */
    public String action;   // Lệnh
    /**
     * Định danh của người gửi hoặc thông tin phụ trợ.
     * Ví dụ: Tên tài khoản (username), Mã phòng (roomId), hoặc "SERVER" nếu từ Server gửi về.
     */
    public String id;

    /**
     * Khối dữ liệu chính (Payload).
     * Khai báo là Object để có thể linh hoạt nhét bất cứ thứ gì vào (String, Double, ArrayList...).
     * Khi nhận được, người nhận phải ÉP KIỂU (Casting) lại cho đúng.
     * VD: String payload = (String) msg.data;
     */
    public Object data;       // Mã
    public String username; // Ai
    public String role;     // Vai trò

    /**
     * Constructor khởi tạo một gói tin Message.
     * @param action Hành động / Lệnh
     * @param id Người gửi / ID ngữ cảnh
     * @param data Dữ liệu đính kèm
     */
    public Message(String action,String id, Object data) {
        this.action = action;
        this.id = id;
        this.data = data;
    }

    //Dành cho sau này, hoặc lúc lỗi
    public Message(String action, String id, String username, Object data) {
        this.action = action;
        this.username = username;
        this.id= id;
        this.data = data;
    }

    public Message(String action,String id, String username, String role, Object data) {
        this.action = action;
        this.id = id;
        this.username = username;
        this.role = role;
        this.data = data;
    }

    // --- GETTER (Dùng khi cần lấy dữ liệu an toàn) ---

    public String getAction() {
        return action;
    }

    public String getId() {
        return id;
    }

    public Object getData() {
        return data;
    }
}