package database;

import java.util.ArrayList;
import java.util.List;

public class AuthDAO {
    private MockDB db = MockDB.getInstance();

    // Lấy danh sách mã phiên đấu giá trả về cho ComboBox
    public List<String> getDanhSachMaPhien() {
        return new ArrayList<>(db.bangGiaSanPham.keySet());
    }

    // Kiểm tra đăng nhập cực nhanh bằng HashMap
    public String checkLogin(String maKhachHang, String maPhien) {
        return "";
    }

    // 1. CREATE: Đăng ký mới (Cấp mã từ 100 đến 999)
    // Sửa lại hàm Đăng ký: Nhận thêm biến 'vaiTro' (BIDDER/SELLER)
    public String registerUser(String tenKhachHang, String vaiTro) {
        return "";
    }

    // Thêm hàm nhỏ này để Server biết user đang đăng nhập là ai
    public String getRole(String maKH) {
        return "";
    }

    // 2. UPDATE: Quên mã / Đổi mã mới hoàn toàn
    public String resetUserId(String name, String contactInfo) {
        String oldId = null;

        // Tìm xem ông này ngày xưa dùng mã nào
        for (java.util.Map.Entry<String, String> entry : db.bangKhachHang.entrySet()) {
            if (entry.getValue().equals(name)) {
                oldId = entry.getKey();
                break;
            }
        }

        // Xóa mã cũ đi
        if (oldId != null) {
            db.bangKhachHang.remove(oldId);
        }

        // Cấp lại mã mới
        return registerUser(name, contactInfo);
    }
}
