package com.auction.common.model;

// Nếu Art kế thừa Item và chung thư mục thì KHÔNG cần import Item.
// Nhưng nếu cần dùng Serializable:

public class Art extends Item {
    private String artistName;

    public Art(String id, String name, String description, double startingPrice, String artistName) {
        super(id, name, description, startingPrice);
        this.artistName = artistName;
    }
    public void printInfo() {
        System.out.println("[Nghệ thuật] " + getName() + " - Tác giả: " + artistName
                + " - Giá hiện tại: $" + getCurrentHighestPrice());
    }
}