package com.auction.client.feature.auth;

/**
 * DTO chứa dữ liệu đăng ký lấy từ FXML.
 * Role SELLER cần organization để admin đánh giá yêu cầu tạo phiên đấu giá.
 */
public record RegisterForm(
        String customerId,
        String username,
        String email,
        String fullName,
        String password,
        String confirmPassword,
        String role,
        String organization
<<<<<<< HEAD
) {
}
=======
) {}
>>>>>>> 79695510de950987573eb4278b356292c3d972f3
