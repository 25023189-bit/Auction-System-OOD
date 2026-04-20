package com.auction.server.service;

import java.time.LocalDateTime;

public class AuctionCreationValidator {
    private String errorMessage = "Thong tin tao phien khong hop le!";

    public boolean validateAuction(String sellerId,
                                   String sellerOrganization,
                                   String itemName,
                                   String itemDesc,
                                   double startingPrice,
                                   double minimumJoinAmount,
                                   double bidStep,
                                   LocalDateTime startTime,
                                   int durationMinutes,
                                   int extensionSeconds) {
        if (sellerId == null || sellerId.isBlank()) {
            errorMessage = "Khong xac dinh duoc seller tao phien!";
            return false;
        }
        if (sellerOrganization == null || sellerOrganization.isBlank()) {
            errorMessage = "Seller phai co to chuc hop le moi duoc tao phien!";
            return false;
        }
        if (itemName == null || itemName.isBlank()) {
            errorMessage = "Ten vat pham khong hop le!";
            return false;
        }
        if (itemDesc == null || itemDesc.isBlank()) {
            errorMessage = "Mo ta vat pham khong duoc de trong!";
            return false;
        }
        if (startingPrice <= 0) {
            errorMessage = "Gia khoi diem phai lon hon 0!";
            return false;
        }
        if (bidStep <= 0) {
            errorMessage = "Buoc gia phai lon hon 0!";
            return false;
        }
        if (!(minimumJoinAmount > startingPrice * 0.10 && minimumJoinAmount < startingPrice * 0.75)) {
            errorMessage = "So tien toi thieu tham gia phai lon hon 10% va nho hon 75% gia khoi diem!";
            return false;
        }
        if (durationMinutes <= 0) {
            errorMessage = "Thoi luong phai lon hon 0 phut!";
            return false;
        }
        if (extensionSeconds < 1 || extensionSeconds > 120) {
            errorMessage = "Gia han phai tu 1 den 120 giay!";
            return false;
        }
        if (startTime == null || startTime.isBefore(LocalDateTime.now())) {
            errorMessage = "Thoi gian bat dau phai o hien tai hoac tuong lai!";
            return false;
        }
        return true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
