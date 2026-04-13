package com.auction.common.model;

// Nếu Art kế thừa Item và chung thư mục thì KHÔNG cần import Item.
// Vì Item đã implements Serializable nên Art cũng tự động được kế thừa.

public class Art extends Item {
    private String artistName;

    // Constructor nhận đủ 5 tham số
    public Art(String id, String name, String description, double startingPrice, String artistName) {
        // Gọi lên class Item cha (cần truyền đúng 4 biến: id, name, description, startingPrice)
        super(id, name, description, startingPrice);
        this.artistName = artistName;
    }

    public void printInfo() {
        // Sử dụng đúng hàm getProductName() và getStartingPrice() từ class Item cha
        System.out.println("[Nghệ thuật] " + getProductName() + " - Tác giả: " + artistName
                + " - Giá khởi điểm: $" + getStartingPrice());
    }

    // --- GETTER & SETTER cho artistName (Thêm vào cho chuẩn bài OOP) ---
    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}