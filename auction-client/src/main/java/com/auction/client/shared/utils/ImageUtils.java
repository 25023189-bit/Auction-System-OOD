package com.auction.client.shared.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ImageUtils {
    private static final int PLACEHOLDER_SIZE = 96;
    private static final Image PLACEHOLDER_IMAGE = createPlaceholderImage();

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

    public static void applyBase64OrPlaceholder(ImageView imageView, String base64String) {
        if (imageView == null) {
            return;
        }
        Image image = decodeBase64ToImage(base64String);
        imageView.setImage(image != null && !image.isError() ? image : PLACEHOLDER_IMAGE);
        configureRoundedImageView(imageView);
    }

    public static void applyImageOrPlaceholder(ImageView imageView, Image image) {
        if (imageView == null) {
            return;
        }
        imageView.setImage(image != null && !image.isError() ? image : PLACEHOLDER_IMAGE);
        configureRoundedImageView(imageView);
    }

    public static void configureRoundedImageView(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageView.fitWidthProperty());
        clip.heightProperty().bind(imageView.fitHeightProperty());
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        imageView.setClip(clip);
    }

    public static Image placeholderImage() {
        return PLACEHOLDER_IMAGE;
    }

    private static Image createPlaceholderImage() {
        WritableImage image = new WritableImage(PLACEHOLDER_SIZE, PLACEHOLDER_SIZE);
        var writer = image.getPixelWriter();
        for (int y = 0; y < PLACEHOLDER_SIZE; y++) {
            for (int x = 0; x < PLACEHOLDER_SIZE; x++) {
                boolean border = x < 3 || y < 3 || x >= PLACEHOLDER_SIZE - 3 || y >= PLACEHOLDER_SIZE - 3;
                boolean diagonal = Math.abs(x - y) < 2 || Math.abs((PLACEHOLDER_SIZE - x) - y) < 2;
                Color color = border || diagonal ? Color.rgb(186, 151, 82) : Color.rgb(246, 248, 252);
                writer.setColor(x, y, color);
            }
        }
        return image;
    }
}
