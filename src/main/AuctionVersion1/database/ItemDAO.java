package database;

public class ItemDAO {
    private MockDB db = MockDB.getInstance();

    // Lấy giá hiện tại
    public double getCurrentPrice(String maPhien) {
        return db.bangGiaSanPham.getOrDefault(maPhien, 0.0);
    }

    // Ghi nhận giá mới và người chiến thắng
    public void updateBid(String maPhien, double newPrice, String winner) {
        db.bangGiaSanPham.put(maPhien, newPrice);
        db.bangKyLuc.put(maPhien, winner);
    }
}