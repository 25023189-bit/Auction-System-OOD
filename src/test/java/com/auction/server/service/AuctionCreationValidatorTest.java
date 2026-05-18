package com.auction.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionCreationValidatorTest {

    private AuctionCreationValidator validator;

    // Các biến chứa dữ liệu HỢP LỆ chuẩn để dùng làm gốc cho mọi bài test
    private String validSellerId = "U001";
    private String validOrg = "UET FITA";
    private String validItemName = "MacBook Pro M3";
    private String validItemDesc = "New 100% sealed";
    private double validStartingPrice = 1000.0;
    private double validMinJoin = 500.0; // Nhỏ hơn 75% của 1000 (tức là < 750)
    private double validBidStep = 50.0;
    private LocalDateTime validStartTime;
    private int validDuration = 60;
    private int validExtension = 30;
    private double validReputation = 4.5;
    private double validSuccessRate = 0.9;
    private double validCancelRate = 0.05;

    @BeforeEach
    void setUp() {
        // Khởi tạo lại Validator mới tinh trước mỗi bài test để không bị lưu state errorMessage
        validator = new AuctionCreationValidator();
        validStartTime = LocalDateTime.now().plusDays(1); // Luôn ở tương lai
    }

    @Test
    @DisplayName("Validate: Thành công khi tất cả dữ liệu đều hợp lệ (Happy Path)")
    void validateAuction_AllValid_ReturnsTrue() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertTrue(isValid, "Validator nên trả về true khi dữ liệu chuẩn.");
    }

    @Test
    @DisplayName("Validate: Thất bại khi Seller ID trống hoặc null")
    void validateAuction_InvalidSellerId_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                "", validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Unable to identify the seller creating this auction.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Seller Organization trống")
    void validateAuction_InvalidOrganization_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, "   ", validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Seller organization is required to create an auction.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Item Name trống")
    void validateAuction_InvalidItemName_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, null, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Item name is invalid.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Item Description trống")
    void validateAuction_InvalidItemDesc_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, "",
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Item description is required.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Starting Price <= 0")
    void validateAuction_InvalidStartingPrice_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                0.0, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Starting price must be greater than 0.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Bid Step <= 0")
    void validateAuction_InvalidBidStep_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, 0.0,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Bid step must be greater than 0.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Minimum Join Amount >= 75% Starting Price")
    void validateAuction_InvalidMinimumJoin_ReturnsFalse() {
        // Giá khởi điểm 1000, 75% là 750. Đặt minJoin = 800 sẽ bị lỗi.
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                1000.0, 800.0, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Minimum join amount must be Less than 75% of the starting price.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Duration <= 0")
    void validateAuction_InvalidDuration_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, 0, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Duration must be greater than 0 minutes.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Extension Seconds nằm ngoài khoảng 1-120")
    void validateAuction_InvalidExtension_ReturnsFalse() {
        // Test extension = 130 (vượt quá 120)
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, 130,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Extension must be between 1 and 120 seconds.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Start Time ở trong quá khứ")
    void validateAuction_InvalidStartTime_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                LocalDateTime.now().minusMinutes(10), validDuration, validExtension,
                validReputation, validSuccessRate, validCancelRate
        );

        assertFalse(isValid);
        assertEquals("Start time must be now or in the future.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Reputation nằm ngoài 0-5")
    void validateAuction_InvalidReputation_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                5.5, validSuccessRate, validCancelRate // Uy tín 5.5 > 5.0
        );

        assertFalse(isValid);
        assertEquals("Seller reputation must be between 0 and 5.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Success Rate nằm ngoài 0-1")
    void validateAuction_InvalidSuccessRate_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, 1.2, validCancelRate // Tỉ lệ thành công 1.2 > 1.0
        );

        assertFalse(isValid);
        assertEquals("Seller successful auction rate must be between 0 and 1.", validator.getErrorMessage());
    }

    @Test
    @DisplayName("Validate: Thất bại khi Admin Cancel Rate nằm ngoài 0-1")
    void validateAuction_InvalidCancelRate_ReturnsFalse() {
        boolean isValid = validator.validateAuction(
                validSellerId, validOrg, validItemName, validItemDesc,
                validStartingPrice, validMinJoin, validBidStep,
                validStartTime, validDuration, validExtension,
                validReputation, validSuccessRate, -0.1 // Tỉ lệ hủy -0.1 < 0.0
        );

        assertFalse(isValid);
        assertEquals("Seller admin cancellation rate must be between 0 and 1.", validator.getErrorMessage());
    }
}