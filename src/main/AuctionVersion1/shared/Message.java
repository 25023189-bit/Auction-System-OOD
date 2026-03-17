package shared;
import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- CÁC MÃ LỆNH CHUẨN (ACTIONS) ---
    public static final String LOGIN = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String BID = "BID";
    public static final String CREATE_AUCTION = "CREATE_AUCTION";
    public static final String CANCEL_AUCTION = "CANCEL_AUCTION";
    public static final String UPDATE_ALL = "UPDATE_ALL"; // Server gửi cho tất cả Client
    public static final String ERROR = "ERROR";

    // --- CÁC BIẾN DỮ LIỆU ---
    public String action;   // Loại hành động
    public String username; // ID người dùng (BD5xxxxx)
    public String role;     // "BIDDER" hoặc "SELLER"
    public Object data;     // Dữ liệu đi kèm (có thể là Item, Auction, hoặc Double)
    public String note;     // Ghi chú hoặc tin nhắn báo lỗi

    // Constructor tổng quát
    public Message(String action, String username, Object data) {
        this.action = action;
        this.username = username;
        this.data = data;
    }

    // Constructor đầy đủ cho Login/Register
    public Message(String action, String username, String role, Object data) {
        this.action = action;
        this.username = username;
        this.role = role;
        this.data = data;
    }
}