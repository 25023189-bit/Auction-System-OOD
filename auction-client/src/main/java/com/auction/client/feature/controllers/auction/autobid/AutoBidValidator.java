package com.auction.client.feature.controllers.auction.autobid;

import com.auction.common.model.AuctionRoom; // Nhớ import model này nhé

public class AutoBidValidator {

    // Đã thêm tham số AuctionRoom room để lấy giá hiện tại và bước giá quy định
    public AutoBidValidationResult validate(AutoBidFormReader.AutoBidFormData formData, AuctionRoom room) {
        if (formData == null || formData.maxBidText().isBlank() || formData.stepText().isBlank()) {
            return AutoBidValidationResult.invalid("He thong: Vui long nhap du Max Bid va buoc gia!\n");
        }

        try {
            double maxBid = Double.parseDouble(formData.maxBidText());
            double step = Double.parseDouble(formData.stepText());

            if (maxBid <= 0 || step <= 0) {
                return AutoBidValidationResult.invalid("He thong: Max Bid va buoc gia phai lon hon 0!\n");
            }

            // --- BẮT ĐẦU THÊM LUẬT MỚI ---
            if (room != null) {
                // Luật 1: Max Bid phải lớn hơn Giá hiện tại
                if (maxBid <= room.getCurrentPrice()) {
                    return AutoBidValidationResult.invalid(
                            "He thong: Loi! Max Bid phai lon hon gia hien tai (" + room.getCurrentPrice() + "$)\n"
                    );
                }

                // Luật 2: Bước giá Auto phải >= Bước giá tối thiểu của phòng
                if (step < room.getBidStep()) {
                    return AutoBidValidationResult.invalid(
                            "He thong: Loi! Buoc gia khong duoc nho hon quy dinh (" + room.getBidStep() + "$)\n"
                    );
                }
            }
            // --- KẾT THÚC THÊM LUẬT MỚI ---

            return AutoBidValidationResult.valid(maxBid, step);

        } catch (NumberFormatException e) {
            return AutoBidValidationResult.invalid("He thong: Loi nhap lieu, vui long nhap so hop le!\n");
        }
    }

    public record AutoBidValidationResult(boolean valid, double maxBid, double step, String message) {
        private static AutoBidValidationResult valid(double maxBid, double step) {
            return new AutoBidValidationResult(true, maxBid, step, null);
        }

        private static AutoBidValidationResult invalid(String message) {
            return new AutoBidValidationResult(false, 0, 0, message);
        }
    }
}