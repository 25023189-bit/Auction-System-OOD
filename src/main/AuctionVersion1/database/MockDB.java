package database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockDB {
    private static MockDB instance;

    public Map<String, String> bangKhachHang = new ConcurrentHashMap<>();
    public Map<String, Double> bangGiaSanPham = new ConcurrentHashMap<>();
    public Map<String, String> bangKyLuc = new ConcurrentHashMap<>();
    public Map<String, String> bangVaiTro = new ConcurrentHashMap<>(); // Lưu "BIDDER" hoặc "SELLER"
    public Map<String, String> bangTenSanPham = new ConcurrentHashMap<>();
    public Map<String, String> bangNguoiBan = new ConcurrentHashMap<>();

    // Design Pattern: Singleton
    public static MockDB getInstance() {
        if (instance == null) {
            instance = new MockDB();
        }
        return instance;
    }

    // Nạp sẵn dữ liệu test
    private MockDB() {
    }
}
