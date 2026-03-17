package shared;
import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemID;      // Định dạng AU1xxxxx
    private String name;
    private double startingPrice;
    private String sellerID;    // ID của Seller tạo ra món hàng này

    public Item(String itemID, String name, double startingPrice, String sellerID) {
        this.itemID = itemID;
        this.name = name;
        this.startingPrice = startingPrice;
        this.sellerID = sellerID;
    }

    // Getters
    public String getItemID() { return itemID; }
    public String getName() { return name; }
    public double getStartingPrice() { return startingPrice; }
    public String getSellerID() { return sellerID; }
}