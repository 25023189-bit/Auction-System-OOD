package database;

public class SellerDAO {
    private MockDB db = MockDB.getInstance();

    // 1. TẠO SẢN PHẨM MỚI
    public String themSanPham(String tenSP, double giaKD, String maNguoiBan) {
        String maPhien;
        do {
            maPhien = "PDG" + (new java.util.Random().nextInt(900) + 100);
        } while (db.bangGiaSanPham.containsKey(maPhien));

        db.bangTenSanPham.put(maPhien, tenSP);
        db.bangGiaSanPham.put(maPhien, giaKD);
        db.bangKyLuc.put(maPhien, "Chưa có");
        db.bangNguoiBan.put(maPhien, maNguoiBan);
        return maPhien;
    }

    // 2. XÓA SẢN PHẨM (Chỉ chủ sở hữu mới được xóa)
    public boolean xoaSanPham(String maPhien, String maNguoiBan) {
        if (maNguoiBan.equals(db.bangNguoiBan.get(maPhien))) {
            db.bangTenSanPham.remove(maPhien);
            db.bangGiaSanPham.remove(maPhien);
            db.bangKyLuc.remove(maPhien);
            db.bangNguoiBan.remove(maPhien);
            return true;
        }
        return false;
    }

    // 3. ĐIỀU CHỈNH GIÁ KHỞI ĐIỂM
    public boolean suaGiaSanPham(String maPhien, double giaMoi, String maNguoiBan) {
        if (maNguoiBan.equals(db.bangNguoiBan.get(maPhien))) {
            // Chỉ cho phép sửa giá khi CHƯA có ai đặt giá (Chưa có kỷ lục)
            if ("Chưa có".equals(db.bangKyLuc.get(maPhien))) {
                db.bangGiaSanPham.put(maPhien, giaMoi);
                return true;
            }
        }
        return false;
    }
}
