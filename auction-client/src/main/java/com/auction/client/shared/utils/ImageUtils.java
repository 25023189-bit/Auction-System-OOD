package com.auction.client.shared.utils;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ImageUtils {

    /**
     * Giải mã chuỗi Base64 thành đối tượng Image của JavaFX để hiển thị lên UI.
     * * @param base64String Chuỗi mã hóa của bức ảnh
     * @return Đối tượng Image, hoặc null nếu chuỗi trống/lỗi
     */
    public static Image decodeBase64ToImage(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null; // Trả về null để UI tự set ảnh mặc định (placeholder)
        }
        try {
            // Dịch ngược Base64 thành mảng byte
            byte[] imageBytes = Base64.getDecoder().decode(base64String);

            // Đưa mảng byte vào luồng và tạo thành Image
            return new Image(new ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            System.err.println("Lỗi giải mã hình ảnh Base64: " + e.getMessage());
            return null;
        }
    }
}