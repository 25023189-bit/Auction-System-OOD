package com.auction.client.feature.controllers.auction.seller;

import com.auction.client.feature.controllers.auction.seller.feedback.SellerFormFeedbackController;
import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormData;
import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormReadResult;
import com.auction.client.feature.controllers.auction.seller.form.SellerAuctionFormReader;
import com.auction.client.feature.controllers.auction.seller.validation.SellerAuctionValidationResult;
import com.auction.client.feature.controllers.auction.seller.validation.SellerAuctionValidator;
import com.auction.client.service.AuctionService;
import com.auction.client.shared.utils.ImageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import javafx.scene.image.ImageView;

public class SellerController {

    @FXML
    private TextField txtItemName;
    @FXML
    private TextArea txtItemDescription;
    @FXML
    private TextField txtStartingPrice;
    @FXML
    private TextField txtMinimumJoinAmount;
    @FXML
    private TextField txtBidStep;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblReputation;
    @FXML
    private DatePicker datePickerStart;
    @FXML
    private TextField txtStartHour;
    @FXML
    private TextField txtStartMinute;
    @FXML
    private TextField txtDuration;
    @FXML
    private TextField txtExtensionSeconds;

    @FXML
    private javafx.scene.image.ImageView imgProductPreview;
    private String base64Image = ""; // Biến chứa ảnh đã mã hóa thành chữ

    private AuctionService auctionService;
    private SellerAuctionFormReader formReader;
    private SellerAuctionValidator validator;
    private SellerFormFeedbackController feedbackController;

    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    @FXML
    public void initialize() {
        formReader = new SellerAuctionFormReader(
                txtItemName, txtItemDescription, txtStartingPrice, txtMinimumJoinAmount, txtBidStep,
                datePickerStart, txtStartHour, txtStartMinute, txtDuration, txtExtensionSeconds
        );
        validator = new SellerAuctionValidator();
        feedbackController = new SellerFormFeedbackController(lblStatus);

        if (txtStartingPrice != null) {
            txtStartingPrice.textProperty().addListener((observable, oldValue, newValue) -> validateJoinAmountRealTime());
        }
        if (txtMinimumJoinAmount != null) {
            txtMinimumJoinAmount.textProperty().addListener((observable, oldValue, newValue) -> validateJoinAmountRealTime());
        }
        if (txtExtensionSeconds != null) {
            txtExtensionSeconds.textProperty().addListener((observable, oldValue, newValue) -> validateExtensionRealTime());
        }
        if (txtDuration != null) {
            txtDuration.textProperty().addListener((observable, oldValue, newValue) -> validateDurationRealTime());
        }
    }

    private void validateJoinAmountRealTime() {
        SellerAuctionFormReadResult readResult = formReader.readJoinAmount();
        if (!readResult.valid()) {
            feedbackController.markNormal(txtMinimumJoinAmount);
            return;
        }
        applyRealtimeFeedback(validator.validateJoinAmount(readResult.data()), txtMinimumJoinAmount);
    }

    private void validateExtensionRealTime() {
        SellerAuctionFormReadResult readResult = formReader.readExtensionSeconds();
        if (!readResult.valid()) {
            feedbackController.error(readResult.message());
            feedbackController.markInvalid(txtExtensionSeconds);
            return;
        }
        applyRealtimeFeedback(validator.validateExtensionSeconds(readResult.data()), txtExtensionSeconds);
    }

    private void validateDurationRealTime() {
        SellerAuctionFormReadResult readResult = formReader.readDuration();
        if (!readResult.valid()) {
            feedbackController.error(readResult.message());
            feedbackController.markInvalid(txtDuration);
            return;
        }
        applyRealtimeFeedback(validator.validateDuration(readResult.data()), txtDuration);
    }

    @FXML
    public void handleCreateAuction() {
        if (auctionService == null) {
            feedbackController.error("Error: Service is not available.");
            return;
        }

        SellerAuctionFormReadResult readResult = formReader.read();
        if (!readResult.valid()) {
            feedbackController.error(readResult.message());
            return;
        }

        SellerAuctionValidationResult validation = validator.validate(readResult.data());
        if (!validation.valid()) {
            feedbackController.error(validation.message());
            return;
        }

        SellerAuctionFormData data = readResult.data();
        if (base64Image == null || base64Image.isBlank()) {
            auctionService.createAuction(
                    data.itemName(),
                    data.itemDescription(),
                    data.startingPrice(),
                    data.minimumJoinAmount(),
                    data.bidStep(),
                    data.startTime(),
                    data.durationMinutes(),
                    data.extensionSeconds()
            );
        } else {
            auctionService.createAuction(
                    data.itemName(),
                    data.itemDescription(),
                    data.startingPrice(),
                    data.minimumJoinAmount(),
                    data.bidStep(),
                    data.startTime(),
                    data.durationMinutes(),
                    data.extensionSeconds(),
                    base64Image
            );
        }

        closeWindow();
    }

    private void applyRealtimeFeedback(SellerAuctionValidationResult validation, TextField field) {
        if (!validation.valid()) {
            feedbackController.error(validation.message());
            feedbackController.markInvalid(field);
            return;
        }
        feedbackController.clear();
        feedbackController.markNormal(field);
    }

    private void closeWindow() {
        javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");

        // Lọc ra chỉ cho phép người dùng chọn file hình ảnh
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Bật cửa sổ chọn file lên
        File selectedFile = fileChooser.showOpenDialog(txtItemName.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Hiển thị ảnh lên khung ImageView cho đẹp
                Image image = new Image(selectedFile.toURI().toString());
                ImageUtils.applyImageOrPlaceholder(imgProductPreview, image);

                // CỰC KỲ QUAN TRỌNG: Băm nhỏ bức ảnh thành chuỗi Base64
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64Image = Base64.getEncoder().encodeToString(fileContent);

                // Tự động xóa dòng báo lỗi (nếu trước đó có lỗi)
                if (feedbackController != null) {
                    feedbackController.clear();
                }

            } catch (Exception e) {
                if (feedbackController != null) {
                    feedbackController.error("Không thể tải ảnh: " + e.getMessage());
                }
            }
        }
    }
}
